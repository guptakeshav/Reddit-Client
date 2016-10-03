package com.keshavg.reddit.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.keshavg.reddit.R;
import com.keshavg.reddit.adapters.CommentsAdapter;
import com.keshavg.reddit.models.CommentResponse;
import com.keshavg.reddit.services.CommentService;

import java.util.Queue;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/13/16.
 */
public class CommentsFragment extends Fragment {
    public static final String URL = "URL";
    public static final String SORT_BY = "SORT_BY";
    public static final String IS_PROFILE_ACTIVITY = "IS_PROFILE_ACTIVITY";

    private String url;
    private String sortByParam;
    private Boolean isProfileActivity;
    private Comments comments;

    private SwipeRefreshLayout swipeContainer;

    @Getter
    private CommentsAdapter commentsAdapter;
    private Button button;
    private ProgressBar progressBarLoadMore;

    private ProgressBar progressBar;

    public static CommentsFragment newInstance(String url, String sortBy, Boolean isProfileActivity) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        args.putString(SORT_BY, sortBy);
        args.putBoolean(IS_PROFILE_ACTIVITY, isProfileActivity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = getArguments().getString(URL);
        sortByParam = getArguments().getString(SORT_BY);
        isProfileActivity = getArguments().getBoolean(IS_PROFILE_ACTIVITY);

        if (isProfileActivity) {
            comments = new CommentsForProfile();
        } else {
            comments = new CommentsForPost();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (getActivity().isFinishing()) {
            return;
        }

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                comments.fetchComments();
            }
        });

        RecyclerView recList = (RecyclerView) view.findViewById(R.id.recycler_list);
        commentsAdapter = new CommentsAdapter(getActivity(), url, sortByParam, isProfileActivity);
        recList.setAdapter(commentsAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recList.setLayoutManager(llm);
        FastScroller fastScroller = (FastScroller) view.findViewById(R.id.fast_scroll);
        fastScroller.setRecyclerView(recList);

        button = (Button) view.findViewById(R.id.button);
        button.setText("Load More");

        progressBar = (ProgressBar) view.findViewById(R.id.progressbar_fragment);
        progressBarLoadMore = (ProgressBar) view.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);

        comments.fetchComments();
    }

    private abstract class Comments {
        abstract void fetchComments();
        abstract void onClickLoadMore(final Queue<String> moreIds);

        void onFetchCommentsSuccessfulResponse(CommentResponse response) {
            // clearing out the previous content
            button.setVisibility(View.GONE);
            commentsAdapter.clear();

            // add the new content
            commentsAdapter.addAll(response.getComments());

            final Queue<String> moreIds = response.getMoreIds();
            if (moreIds != null && !moreIds.isEmpty()) {
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(v -> onClickLoadMore(moreIds));
            }

            onFetchCommentsComplete();
        }

        void onFetchCommentsComplete() {
            progressBar.setVisibility(View.GONE);
            swipeContainer.setRefreshing(false);
        }

        void onLoadMoreSuccessfulResponse(CommentResponse response, Queue<String> moreIds) {
            commentsAdapter.addAll(response.getComments());

            progressBarLoadMore.setVisibility(View.GONE);
            moreIds.remove();
            if (!moreIds.isEmpty()) {
                button.setVisibility(View.VISIBLE);
            }
        }

        void onLoadMoreUnsuccess() {
            progressBarLoadMore.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }

    }

    private class CommentsForPost extends Comments {
        /**
         * Function to fetch the list of comments from the REST api
         */
        public void fetchComments() {
            final CommentService commentService = new CommentService();
            commentService.fetchCommentsForPosts(
                    getContext(),
                    url,
                    "",
                    sortByParam,
                    () -> onFetchCommentsSuccessfulResponse(commentService.getCommentResponse()),
                    this::onFetchCommentsComplete
            );
        }

        /**
         * On click listener for the load more button
         * Fetches more comments from the REST api and adds to the adapter
         * @param moreIds
         */
        public void onClickLoadMore(final Queue<String> moreIds) {
            button.setVisibility(View.GONE);
            progressBarLoadMore.setVisibility(View.VISIBLE);

            final CommentService commentService = new CommentService();
            commentService.fetchCommentsForPosts(
                    getContext(),
                    url,
                    moreIds.peek(),
                    sortByParam,
                    () -> onLoadMoreSuccessfulResponse(commentService.getCommentResponse(), moreIds),
                    () -> onLoadMoreUnsuccess()
            );
        }
    }

    private class CommentsForProfile extends Comments {
        /**
         * Function to fetch the list of comments from the REST api
         */
        public void fetchComments() {
            final CommentService commentService = new CommentService();
            commentService.fetchCommentsForProfile(
                    getContext(),
                    url,
                    "",
                    () -> onFetchCommentsSuccessfulResponse(commentService.getCommentResponse()),
                    () -> onFetchCommentsComplete()
            );
        }

        /**
         * On click listener for the load more button
         * Fetches more comments from the REST api and adds to the adapter
         * @param moreIds
         */
        public void onClickLoadMore(final Queue<String> moreIds) {
            button.setVisibility(View.GONE);
            progressBarLoadMore.setVisibility(View.VISIBLE);

            final CommentService commentService = new CommentService();
            commentService.fetchCommentsForProfile(
                    getContext(),
                    url,
                    moreIds.peek(),
                    () -> onLoadMoreSuccessfulResponse(commentService.getCommentResponse(), moreIds),
                    () -> onLoadMoreUnsuccess()
            );
        }
    }
}
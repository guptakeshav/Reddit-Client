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
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.keshavg.reddit.R;
import com.keshavg.reddit.adapters.CommentsAdapter;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.models.CommentResponse;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;

import java.util.List;
import java.util.Queue;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshavgupta on 9/13/16.
 */
public class CommentsFragment extends Fragment {
    private String url;
    private String sortByParam;
    private Boolean isProfileActivity;
    private Comments comments;

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recList;
    private FastScroller fastScroller;

    @Getter
    private CommentsAdapter commentsAdapter;
    private LinearLayoutManager llm;
    private Button button;
    private ProgressBar progressBarLoadMore;

    private ProgressBar progressBar;

    public static CommentsFragment newInstance(String url, String sortBy, Boolean isProfileActivity) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        args.putString("SORT_BY", sortBy);
        args.putBoolean("IS_PROFILE_ACTIVITY", isProfileActivity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = getArguments().getString("URL");
        sortByParam = getArguments().getString("SORT_BY");
        isProfileActivity = getArguments().getBoolean("IS_PROFILE_ACTIVITY");

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

        recList = (RecyclerView) view.findViewById(R.id.recycler_list);
        commentsAdapter = new CommentsAdapter(getActivity(), url, sortByParam, isProfileActivity);
        recList.setAdapter(commentsAdapter);
        llm = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recList.setLayoutManager(llm);
        fastScroller = (FastScroller) view.findViewById(R.id.fast_scroll);
        fastScroller.setRecyclerView(recList);

        button = (Button) view.findViewById(R.id.button);
        button.setText("Load More");

        progressBar = (ProgressBar) view.findViewById(R.id.progressbar_fragment);
        progressBarLoadMore = (ProgressBar) view.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);

        comments.fetchComments();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private abstract class Comments {
        abstract void fetchComments();
        abstract void onClickLoadMore(final Queue<String> moreIds);

        public void onFetchCommentsSuccessfulResponse(CommentResponse response) {
            // clearing out the previous content
            button.setVisibility(View.GONE);
            commentsAdapter.clear();

            // add the new content
            commentsAdapter.addAll(response.getComments());

            final Queue<String> moreIds = response.getMoreIds();
            if (moreIds != null && !moreIds.isEmpty()) {
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickLoadMore(moreIds);
                    }
                });
            }
        }

        public void onFetchCommentsComplete() {
            progressBar.setVisibility(View.GONE);
            swipeContainer.setRefreshing(false);
        }

        public void onLoadMoreSuccessfulResponse(CommentResponse response, Queue<String> moreIds) {
            commentsAdapter.addAll(response.getComments());

            progressBarLoadMore.setVisibility(View.GONE);
            moreIds.remove();
            if (!moreIds.isEmpty()) {
                button.setVisibility(View.VISIBLE);
            }
        }

        public void onLoadMoreUnsuccess(String message) {
            showToast(message);
            progressBarLoadMore.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }

    }

    private class CommentsForPost extends Comments {
        private Call<List<CommentResponse>> getRetrofitCall(String moreId) {
            RedditApiInterface apiService;
            Call<List<CommentResponse>> call;

            if (AuthSharedPrefHelper.isLoggedIn()) {
                apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
                call = apiService.getOAuthComments(
                        "bearer " + AuthSharedPrefHelper.getAccessToken(),
                        url,
                        moreId,
                        sortByParam,
                        1
                );
            } else {
                apiService = RedditApiClient.getClient().create(RedditApiInterface.class);
                call = apiService.getComments(url, moreId, sortByParam, 1);
            }

            return call;
        }

        /**
         * Function to fetch the list of comments from the REST api
         */
        public void fetchComments() {
            Call<List<CommentResponse>> call = getRetrofitCall("");
            call.enqueue(new Callback<List<CommentResponse>>() {
                @Override
                public void onResponse(Call<List<CommentResponse>> call, Response<List<CommentResponse>> response) {
                    if (response.isSuccessful()) {
                        onFetchCommentsSuccessfulResponse(response.body().get(1));
                    } else {
                        showToast("Comments - " + response.message());
                    }

                    onFetchCommentsComplete();
                }

                @Override
                public void onFailure(Call<List<CommentResponse>> call, Throwable t) {
                    showToast(getString(R.string.error_server_connect));
                    onFetchCommentsComplete();
                }
            });
        }

        /**
         * On click listener for the load more button
         * Fetches more comments from the REST api and adds to the adapter
         * @param moreIds
         */
        public void onClickLoadMore(final Queue<String> moreIds) {
            button.setVisibility(View.GONE);
            progressBarLoadMore.setVisibility(View.VISIBLE);

            Call<List<CommentResponse>> callMore = getRetrofitCall(moreIds.peek());
            callMore.enqueue(new Callback<List<CommentResponse>>() {
                @Override
                public void onResponse(Call<List<CommentResponse>> call, Response<List<CommentResponse>> response) {
                    if (response.isSuccessful()) {
                        onLoadMoreSuccessfulResponse(response.body().get(1), moreIds);
                    } else {
                        onLoadMoreUnsuccess(response.message());
                    }
                }

                @Override
                public void onFailure(Call<List<CommentResponse>> call, Throwable t) {
                    onLoadMoreUnsuccess(getString(R.string.error_server_connect));
                }
            });
        }
    }

    private class CommentsForProfile extends Comments {
        private Call<CommentResponse> getRetrofitCall(String moreId) {
            RedditApiInterface apiService;
            Call<CommentResponse> call;
            if (AuthSharedPrefHelper.isLoggedIn()) {
                apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
                call = apiService.getOAuthProfileComments(
                        "bearer " + AuthSharedPrefHelper.getAccessToken(),
                        url,
                        moreId,
                        "",
                        1
                );
            } else {
                apiService = RedditApiClient.getClient().create(RedditApiInterface.class);
                call = apiService.getProfileComments(url, moreId, "", 1);
            }

            return call;
        }

        /**
         * Function to fetch the list of comments from the REST api
         */
        public void fetchComments() {
            Call<CommentResponse> call = getRetrofitCall("");
            call.enqueue(new Callback<CommentResponse>() {
                @Override
                public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                    if (response.isSuccessful()) {
                        onFetchCommentsSuccessfulResponse(response.body());
                    } else {
                        showToast("Comments - " + response.message());
                    }

                    onFetchCommentsComplete();
                }

                @Override
                public void onFailure(Call<CommentResponse> call, Throwable t) {
                    showToast(getString(R.string.error_server_connect));
                    onFetchCommentsComplete();
                }
            });
        }

        /**
         * On click listener for the load more button
         * Fetches more comments from the REST api and adds to the adapter
         * @param moreIds
         */
        public void onClickLoadMore(final Queue<String> moreIds) {
            button.setVisibility(View.GONE);
            progressBarLoadMore.setVisibility(View.VISIBLE);

            Call<CommentResponse> callMore = getRetrofitCall(moreIds.peek());
            callMore.enqueue(new Callback<CommentResponse>() {
                @Override
                public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                    if (response.isSuccessful()) {
                        onLoadMoreSuccessfulResponse(response.body(), moreIds);
                    } else {
                        onLoadMoreUnsuccess(response.message());
                    }
                }

                @Override
                public void onFailure(Call<CommentResponse> call, Throwable t) {
                    onLoadMoreUnsuccess(getString(R.string.error_server_connect));
                }
            });
        }
    }
}
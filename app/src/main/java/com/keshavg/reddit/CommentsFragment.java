package com.keshavg.reddit;

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

import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshavgupta on 9/13/16.
 */
public class CommentsFragment extends Fragment {
    private String url;
    private String sortByParam;

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recList;
    private CommentsAdapter commentsAdapter;
    private LinearLayoutManager llm;
    private Button button;
    private ProgressBar progressBar;

    private ProgressBar progressBarActivity;

    public static CommentsFragment newInstance(String url, String sortBy) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putString("sortBy", sortBy);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = getArguments().getString("url");
        sortByParam = getArguments().getString("sortBy");
        progressBarActivity = (ProgressBar) getActivity().findViewById(R.id.progressbar_comments);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                commentsAdapter.clear();
                fetchComments();
            }
        });

        recList = (RecyclerView) view.findViewById(R.id.recycler_list);
        commentsAdapter = new CommentsAdapter(getContext(), url, sortByParam);
        recList.setAdapter(commentsAdapter);
        llm = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recList.setLayoutManager(llm);

        button = (Button) view.findViewById(R.id.button);
        button.setText("Load More");
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);

        fetchComments();
    }

    /**
     * Function to fetch the list of comments from the REST api
     */
    public void fetchComments() {
        progressBarActivity.setVisibility(View.VISIBLE);

        final ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<CommentResponse> call = apiService.getComments(url, sortByParam);

        call.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                commentsAdapter.addAll(response.body().getComments());

                final Queue<String> moreIds = response.body().getMoreIds();
                if (!moreIds.isEmpty()) {
                    button.setVisibility(View.VISIBLE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickLoadMore(moreIds);
                        }
                    });
                }

                progressBarActivity.setVisibility(View.GONE);
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error fetching the list of comments", Toast.LENGTH_SHORT)
                        .show();

                progressBarActivity.setVisibility(View.GONE);
                swipeContainer.setRefreshing(false);
            }
        });
    }

    /**
     * On click listener for the load more button
     * Fetches more comments from the REST api and adds to the adapter
     * @param moreIds
     */
    private void onClickLoadMore(final Queue<String> moreIds) {
        button.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<CommentResponse> callMore =
                apiService.getMoreComments(url, sortByParam, moreIds.peek());

        callMore.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                commentsAdapter.addAll(response.body().getComments());

                progressBar.setVisibility(View.GONE);
                moreIds.remove();
                if (!moreIds.isEmpty()) {
                    button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error fetching the list of comments", Toast.LENGTH_SHORT)
                        .show();

                progressBar.setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
            }
        });
    }
}
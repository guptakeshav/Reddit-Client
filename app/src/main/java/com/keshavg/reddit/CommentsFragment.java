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

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recList;

    @Getter
    private CommentsAdapter commentsAdapter;
    private LinearLayoutManager llm;
    private Button button;
    private ProgressBar progressBar;

    private ProgressBar progressBarActivity;

    public static CommentsFragment newInstance(String url, String sortBy) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        args.putString("SORT_BY", sortBy);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = getArguments().getString("URL");
        sortByParam = getArguments().getString("SORT_BY");
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
                commentsAdapter.clear();
                fetchComments();
            }
        });

        recList = (RecyclerView) view.findViewById(R.id.recycler_list);
        commentsAdapter = new CommentsAdapter(getActivity(), url, sortByParam);
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
        progressBarActivity = (ProgressBar) getActivity().findViewById(R.id.progressbar_comments);
        progressBarActivity.setVisibility(View.VISIBLE);

        fetchComments();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Function to fetch the list of comments from the REST api
     */
    public void fetchComments() {
        ApiInterface apiService;
        Call<List<CommentResponse>> call;

        if (MainActivity.AuthPrefManager.isLoggedIn()) {
            apiService = ApiClient.getOAuthClient().create(ApiInterface.class);
            call = apiService.getOAuthComments(
                    "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                    url,
                    sortByParam,
                    1
            );
        } else {
            apiService = ApiClient.getClient().create(ApiInterface.class);
            call = apiService.getComments(url, sortByParam, 1);
        }

        call.enqueue(new Callback<List<CommentResponse>>() {
            @Override
            public void onResponse(Call<List<CommentResponse>> call, Response<List<CommentResponse>> response) {
                if (response.isSuccessful()) {
                    commentsAdapter.addAll(response.body().get(1).getComments());

                    final Queue<String> moreIds = response.body().get(1).getMoreIds();
                    if (moreIds != null && !moreIds.isEmpty()) {
                        button.setVisibility(View.VISIBLE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onClickLoadMore(moreIds);
                            }
                        });
                    }
                } else {
                    showToast("Comments - " + response.message());
                }

                onComplete();
            }

            @Override
            public void onFailure(Call<List<CommentResponse>> call, Throwable t) {
                showToast(getString(R.string.server_error));
                onComplete();
            }

            private void onComplete() {
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

        ApiInterface apiService;
        Call<List<CommentResponse>> callMore;

        if (MainActivity.AuthPrefManager.isLoggedIn()) {
            apiService = ApiClient.getOAuthClient().create(ApiInterface.class);
            callMore = apiService.getMoreOAuthComments(
                    "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                    url,
                    moreIds.peek(),
                    sortByParam,
                    1
            );
        } else {
            apiService = ApiClient.getClient().create(ApiInterface.class);
            callMore = apiService.getMoreComments(url, moreIds.peek(), sortByParam, 1);
        }

        callMore.enqueue(new Callback<List<CommentResponse>>() {
            private void onUnsuccessfulCall(String message) {
                showToast(message);
                progressBar.setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(Call<List<CommentResponse>> call, Response<List<CommentResponse>> response) {
                if (response.isSuccessful()) {
                    commentsAdapter.addAll(response.body().get(1).getComments());

                    progressBar.setVisibility(View.GONE);
                    moreIds.remove();
                    if (!moreIds.isEmpty()) {
                        button.setVisibility(View.VISIBLE);
                    }
                } else {
                    onUnsuccessfulCall(response.message());
                }
            }

            @Override
            public void onFailure(Call<List<CommentResponse>> call, Throwable t) {
                onUnsuccessfulCall(getString(R.string.server_error));
            }
        });
    }
}
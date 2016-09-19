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
import android.widget.ProgressBar;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshav.g on 22/08/16.
 */
public class SubredditsFragment extends Fragment {
    private Boolean loadingFlag;

    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBarActivity;
    private RecyclerView recList;
    private SubredditsAdapter subredditsAdapter;
    private LinearLayoutManager llm;
    private ProgressBar progressBarLoadMore;

    private String searchQuery;
    private String afterParam;

    public SubredditsFragment() {
    }

    public static SubredditsFragment newInstance(String searchQuery) {
        SubredditsFragment fragment = new SubredditsFragment();
        Bundle args = new Bundle();
        args.putString("searchQuery", searchQuery);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchQuery = getArguments().getString("searchQuery");
        loadingFlag = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_list, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        recList = (RecyclerView) rootView.findViewById(R.id.recycler_list);
        subredditsAdapter = new SubredditsAdapter(getActivity());
        recList.setAdapter(subredditsAdapter);
        llm = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recList.setLayoutManager(llm);
        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                loadMoreSubreddits();
            }
        });

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchSubreddits(true, progressBarLoadMore);
            }
        });

        progressBarActivity = (ProgressBar) getActivity().findViewById(R.id.progressbar_posts);
        progressBarLoadMore = (ProgressBar) getActivity().findViewById(R.id.progressbar_loadmore);
        fetchSubreddits(true, progressBarActivity);
    }

    /**
     * Fetching more subreddits when the end of list has reached
     */
    private void loadMoreSubreddits() {
        int totalItemCount = llm.getItemCount();
        int lastVisibleItem = llm.findLastVisibleItemPosition();
        int visibleThreshold = 2;

        if (!loadingFlag && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
            fetchSubreddits(false, progressBarLoadMore);
        }
    }

    public void fetchSubreddits(final Boolean clearAdapterFlag, final ProgressBar progressBar) {
        loadingFlag = true;
        progressBar.setVisibility(View.VISIBLE);

        if (clearAdapterFlag == true) {
            afterParam = "";
        }

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<SubredditResponse> call = apiService.getSubreddits(searchQuery, afterParam);
        call.enqueue(new Callback<SubredditResponse>() {
            private void onUnsuccessfulCall(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT)
                        .show();

                onComplete();
            }

            @Override
            public void onResponse(Call<SubredditResponse> call, Response<SubredditResponse> response) {
                if (response.isSuccessful()) {
                    if (clearAdapterFlag == true) {
                        subredditsAdapter.clear();
                    }

                    afterParam = response.body().getAfterParam();
                    subredditsAdapter.addAll(response.body().getSubreddits());

                    onComplete();
                } else {
                    onUnsuccessfulCall(response.message());
                }
            }

            @Override
            public void onFailure(Call<SubredditResponse> call, Throwable t) {
                onUnsuccessfulCall(getString(R.string.server_error));
            }

            private void onComplete() {
                progressBar.setVisibility(View.GONE);
                swipeContainer.setRefreshing(false);
                loadingFlag = false;
            }
        });
    }
}
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.keshavg.reddit.activities.MainActivity;
import com.keshavg.reddit.adapters.PostsAdapter;
import com.keshavg.reddit.db.PostsDbHelper;
import com.keshavg.reddit.R;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;
import com.keshavg.reddit.models.Post;
import com.keshavg.reddit.models.PostResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshav.g on 22/08/16.
 */
public class PostsFragment extends Fragment {
    private Boolean loadingFlag;

    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private RecyclerView recList;
    private PostsAdapter postsAdapter;
    private LinearLayoutManager llm;
    private ProgressBar progressBarLoadMore;
    private FastScroller fastScroller;

    private String url;
    private String sortByParam;
    private String afterParam;
    private int isSearch;
    private Boolean isHiddenPostsShown;
    private Boolean clearOnHide;
    private Boolean clearOnUnHide;

    private PostsDbHelper dbHelper;

    public PostsFragment() {
    }

    public static PostsFragment newInstance(String url,
                                            String sortByParam,
                                            int isSearch,
                                            Boolean isHiddenPostsShown,
                                            Boolean clearOnHide,
                                            Boolean clearOnUnHide) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        args.putString("SORT_BY", sortByParam);
        args.putInt("IS_SEARCH", isSearch);
        args.putBoolean("IS_HIDDEN_POSTS_SHOWN", isHiddenPostsShown);
        args.putBoolean("CLEAR_ON_HIDE", clearOnHide);
        args.putBoolean("CLEAR_ON_UNHIDE", clearOnUnHide);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = getArguments().getString("URL");
        sortByParam = getArguments().getString("SORT_BY");
        isSearch = getArguments().getInt("IS_SEARCH");
        loadingFlag = false;
        isHiddenPostsShown = getArguments().getBoolean("IS_HIDDEN_POSTS_SHOWN");
        clearOnHide = getArguments().getBoolean("CLEAR_ON_HIDE");
        clearOnUnHide = getArguments().getBoolean("CLEAR_ON_UNHIDE");

        dbHelper = new PostsDbHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_list, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        recList = (RecyclerView) rootView.findViewById(R.id.recycler_list);
        postsAdapter = new PostsAdapter(
                getActivity(),
                Glide.with(getContext()),
                clearOnHide,
                clearOnUnHide);
        recList.setAdapter(postsAdapter);
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
                loadMorePosts();
            }
        });
        fastScroller = (FastScroller) rootView.findViewById(R.id.fast_scroll);
        fastScroller.setRecyclerView(recList);

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPosts(true, progressBarLoadMore);
            }
        });

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressbar_fragment);
        progressBarLoadMore = (ProgressBar) rootView.findViewById(R.id.progressbar_bottom);

        fetchPosts(true, progressBar);
    }

    /**
     * Fetching more posts when the end of list has reached
     */
    private void loadMorePosts() {
        if (afterParam == null) {
            return;
        }

        int totalItemCount = llm.getItemCount();
        int lastVisibleItem = llm.findLastVisibleItemPosition();
        int visibleThreshold = 2;

        if (!loadingFlag && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
            fetchPosts(false, progressBarLoadMore);
        }
    }

    /**
     * Function to fetch the list of posts from the REST api
     * @param clearAdapterFlag
     */
    public void fetchPosts(final Boolean clearAdapterFlag, final ProgressBar progressBar) {
        loadingFlag = true;
        progressBar.setVisibility(View.VISIBLE);

        if (clearAdapterFlag) {
            afterParam = "";
        }

        RedditApiInterface apiService;
        Call<PostResponse> call;

        if (isSearch == 0) { // not performing search operation
            String completeUrl = url.equals("") ? sortByParam : (url + "/" + sortByParam);
            if (MainActivity.AuthPrefManager.isLoggedIn()) {

                apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
                call = apiService.getOAuthPosts(
                        "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                        completeUrl,
                        afterParam
                );
            } else {

                apiService = RedditApiClient.getClient().create(RedditApiInterface.class);
                call = apiService.getPosts(completeUrl, afterParam);
            }
        } else {
            if (MainActivity.AuthPrefManager.isLoggedIn()) {

                apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
                call = apiService.searchOAuthPosts(
                        "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                        url,
                        sortByParam,
                        afterParam
                );
            } else {

                apiService = RedditApiClient.getClient().create(RedditApiInterface.class);
                call = apiService.searchPosts(url, sortByParam, afterParam);
            }
        }

        call.enqueue(new Callback<PostResponse>() {
            private void onUnsuccessfulCall(String message) {
                showToast(message);

                if (clearAdapterFlag) {
                    postsAdapter.clear();
                    postsAdapter.addAll(dbHelper.getPosts(url + "/" + sortByParam), isHiddenPostsShown);
                }

                onComplete();
            }

            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful()) {
                    if (clearAdapterFlag) {
                        postsAdapter.clear();
                        dbHelper.clearTable();
                    }

                    response.body().fixPermalink();
                    response.body().fixImage();

                    List<Post> posts = response.body().getPosts();
                    afterParam = response.body().getAfterId();

                    postsAdapter.addAll(posts, isHiddenPostsShown);
                    dbHelper.insertPosts(posts, url + "/" + sortByParam);

                    onComplete();
                } else {
                    onUnsuccessfulCall("Posts - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                onUnsuccessfulCall(getString(R.string.server_error));
            }

            private void onComplete() {
                swipeContainer.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                loadingFlag = false;
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
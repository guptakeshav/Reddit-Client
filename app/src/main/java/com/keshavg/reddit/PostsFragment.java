package com.keshavg.reddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.futuremind.recyclerviewfastscroll.FastScroller;

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

    private RedditPostsDbHelper dbHelper;

    public PostsFragment() {
    }

    public static PostsFragment newInstance(String url, String sortByParam, int isSearch) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        args.putString("SORT_BY", sortByParam);
        args.putInt("IS_SEARCH", isSearch);
        fragment.setArguments(args);
        return fragment;
    }

    private class PostTouchHelper extends ItemTouchHelper.SimpleCallback {
        // TODO
        public PostTouchHelper() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            postsAdapter.remove(viewHolder.getAdapterPosition());
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = getArguments().getString("URL");
        sortByParam = getArguments().getString("SORT_BY");
        isSearch = getArguments().getInt("IS_SEARCH");
        loadingFlag = false;

        dbHelper = new RedditPostsDbHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_list, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        recList = (RecyclerView) rootView.findViewById(R.id.recycler_list);
        postsAdapter = new PostsAdapter(getActivity(), Glide.with(getContext()));
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

        ItemTouchHelper.Callback callback = new PostTouchHelper();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recList);

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

        if (clearAdapterFlag == true) {
            afterParam = "";
        }

        ApiInterface apiService;
        Call<PostResponse> call;

        if (isSearch == 0) { // not performing search operation
            String completeUrl = url.equals("") ? sortByParam : (url + "/" + sortByParam);
            if (MainActivity.AuthPrefManager.isLoggedIn()) {

                apiService = ApiClient.getOAuthClient().create(ApiInterface.class);
                call = apiService.getOAuthPosts(
                        "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                        completeUrl,
                        afterParam
                );
            } else {

                apiService = ApiClient.getClient().create(ApiInterface.class);
                call = apiService.getPosts(completeUrl, afterParam);
            }
        } else {
            if (MainActivity.AuthPrefManager.isLoggedIn()) {

                apiService = ApiClient.getOAuthClient().create(ApiInterface.class);
                call = apiService.searchOAuthPosts(
                        "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                        url,
                        sortByParam,
                        afterParam
                );
            } else {

                apiService = ApiClient.getClient().create(ApiInterface.class);
                call = apiService.searchPosts(url, sortByParam, afterParam);
            }
        }

        call.enqueue(new Callback<PostResponse>() {
            private void onUnsuccessfulCall(String message) {
                showToast(message);

                if (clearAdapterFlag == true) {
                    postsAdapter.clear();
                    postsAdapter.addAll(dbHelper.getPosts(url + "/" + sortByParam));
                }

                onComplete();
            }

            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful()) {
                    if (clearAdapterFlag == true) {
                        postsAdapter.clear();
                        dbHelper.clearTable();
                    }

                    List<Post> posts = response.body().getPosts();
                    afterParam = response.body().getAfterId();

                    postsAdapter.addAll(posts);
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
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
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
    private LinearLayoutManager llm;

    private List<Post> posts;
    private PostsAdapter postsAdapter;

    private String url;
    private String sortByParam;
    private String afterParam;

    private RedditPostsDbHelper dbHelper;

    public PostsFragment() {
    }

    public static PostsFragment newInstance(String url, String sortBy) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putString("sortBy", sortBy);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = getArguments().getString("url");
        sortByParam = getArguments().getString("sortBy");
        loadingFlag = false;
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_posts);

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

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPosts(true);
            }
        });

        fetchPosts(true);
    }

    /**
     * Fetching more posts when the end of list has reached
     */
    private void loadMorePosts() {
        int totalItemCount = llm.getItemCount();
        int lastVisibleItem = llm.findLastVisibleItemPosition();
        int visibleThreshold = 2;

        if (!loadingFlag && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
            fetchPosts(false);
        }
    }

    public void fetchPosts(final Boolean clearAdapterFlag) {
        loadingFlag = true;
        progressBar.setVisibility(View.VISIBLE);
        posts = new ArrayList<>();

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<PostResponse> call;
        if (clearAdapterFlag == true) {
            call = apiService.getPosts(url, sortByParam);
        } else {
            call = apiService.getPostsAfter(url, sortByParam, afterParam);
        }

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                afterParam = response.body().getAfterParam();
                posts = response.body().getPosts();

                if (clearAdapterFlag == true) {
                    postsAdapter.clear();
                    dbHelper.removePosts(url + "/" + sortByParam);
                }

                postsAdapter.addAll(posts);
                dbHelper.insertPosts(posts, url + "/" + sortByParam);

                progressBar.setVisibility(View.GONE);
                swipeContainer.setRefreshing(false);
                loadingFlag = false;
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error fetching the list of posts", Toast.LENGTH_SHORT)
                        .show();

                if (clearAdapterFlag == true) {
                    postsAdapter.clear();
                    postsAdapter.addAll(dbHelper.getPosts(url + "/" + sortByParam));
                }

                progressBar.setVisibility(View.GONE);
                swipeContainer.setRefreshing(false);
                loadingFlag = false;
            }
        });
    }
}
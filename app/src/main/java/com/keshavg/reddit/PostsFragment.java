package com.keshavg.reddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    private String afterParam;

    public PostsFragment() {
    }

    public static PostsFragment newInstance(String url) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingFlag = false;
        posts = new ArrayList<Post>();
        postsAdapter = new PostsAdapter(getContext(), posts);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar);
        fetchNewPosts(getArguments().getString("url"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_post, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        postsAdapter = new PostsAdapter(getContext(), posts);

        recList = (RecyclerView) rootView.findViewById(R.id.posts_list);
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

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.posts_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewPosts(url);
            }
        });
    }

    /**
     * Fetching more posts when the end of list has reached
     */
    private void loadMorePosts() {
        int totalItemCount = llm.getItemCount();
        int lastVisibleItem = llm.findLastVisibleItemPosition();
        int visibleThreshold = 2;

        if (!loadingFlag && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
            String paramUrl = url + "/" + afterParam;

            try {
                fetchPosts(paramUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get all posts from the starting for a particular link
     *
     * @param url
     */
    public void fetchNewPosts(String url) {
        this.url = url;
        postsAdapter.clear();

        try {
            fetchPosts(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Making calls to the API and fetching required data
     *
     * @param url
     * @throws IOException
     */
    public void fetchPosts(String url) throws IOException {

        /**
         * Indicating that the posts are being fetched
         */
        loadingFlag = true;
        progressBar.setVisibility(View.VISIBLE);

        posts = new ArrayList<Post>();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = new OkHttpClient().newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    afterParam = jsonObject.getString("after");
                    JSONArray redditPosts = jsonObject.getJSONArray("data");

                    for (int idx = 0; idx < redditPosts.length(); ++idx) {
                        JSONObject currentPost = redditPosts.getJSONObject(idx);
                        Post post = new Post(
                                currentPost.getString("author"),
                                currentPost.getInt("created"),
                                currentPost.getInt("num_comments"),
                                currentPost.getString("permalink"),
                                currentPost.getInt("score"),
                                currentPost.getString("subreddit"),
                                currentPost.getString("thumbnail"),
                                currentPost.getString("title"),
                                currentPost.getString("url")
                        );

                        posts.add(post);

                        /**
                         * Logging posts title
                         */
                        Log.d("Post URL #" + idx + " ", post.getTitle());
                    }

                    /**
                     * Updating the UI on async fetching of posts
                     */
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            postsAdapter.addAll(posts);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    /**
                     * Fetching of posts is completed
                     */
                    loadingFlag = false;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            swipeContainer.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }
}

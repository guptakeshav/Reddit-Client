package com.keshavg.reddit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import static com.keshavg.reddit.Constants.BASE_URL;

/**
 * Created by keshav.g on 22/08/16.
 */
public class PostsFragment extends Fragment {

    private Boolean initFlag, loadingFlag;

    private RecyclerView recList;
    private PostsAdapter postsAdapter;
    private List<Post> posts;

    private String url;
    private String afterParam;

    public PostsFragment() {
        initFlag = false;
        loadingFlag = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_main, container, false);
        recList = (RecyclerView) rootView.findViewById(R.id.posts_list);

        final LinearLayoutManager llm = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recList.setLayoutManager(llm);

        /**
         * Fetching "hot" posts
         * It will be shown on the first activity, on opening the app
         */
        url = BASE_URL + "/api/v1/hot";

        try {
            fetchPosts(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * Adding more posts to the list, at the end of scroll
         */
        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

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
        });

        return rootView;
    }

    public void getNewPosts(String url) {
        this.url = url;
        initFlag = false;

        try {
            fetchPosts(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchPosts(String url) throws IOException {

        /**
         * Indicating that the posts are being fetched
         */
        loadingFlag = true;

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
                         * Logging Posts URL
                         */
                        Log.d("Post URL #" + idx + " ", post.getTitle());
                    }

                    /**
                     * Updating the UI on async fetching of posts
                     */
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (initFlag == false) {
                                postsAdapter = new PostsAdapter(getContext(), posts);
                                recList.setAdapter(postsAdapter);
                                initFlag = true;
                            } else {
                                for (Post post : posts) {
                                    postsAdapter.addItem(post);
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    /**
                     * Fetching of posts is completed
                     */
                    loadingFlag = false;
                }
            }
        });
    }
}

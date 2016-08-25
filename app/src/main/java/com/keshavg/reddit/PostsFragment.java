package com.keshavg.reddit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

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

    private Boolean initFlag, loadingFlag;
    private ListView listView;
    private PostsAdapter postsAdapter;
    private List<Post> posts;
    private String afterParam;

    public PostsFragment() {
        initFlag = false;
        loadingFlag = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String url = "http://f9591e36.ngrok.io/api/v1/hot";

        try {
            fetchPosts(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        View rootView = inflater.inflate(R.layout.content_main, container, false);
        listView = (ListView) rootView.findViewById(R.id.posts_list);

        /**
         * Adding more posts to the list, at the end of scroll
         */
        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    String url = "http://f9591e36.ngrok.io/api/v1/hot/" + afterParam;

                    try {
                        if (loadingFlag == false) {
                            loadingFlag = true;
                            fetchPosts(url);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return rootView;
    }

    public void fetchPosts(String url) throws IOException {
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
                    String HTML = response.body().string();
                    JSONObject jsonObject = new JSONObject(HTML);

                    afterParam = jsonObject.getString("after");
                    JSONArray redditPosts = jsonObject.getJSONArray("data");

                    for (int idx = 0; idx < redditPosts.length(); ++idx) {
                        JSONObject currentPost = redditPosts.getJSONObject(idx);
                        Post post = new Post(
                                currentPost.getString("author"),
                                currentPost.getInt("created"),
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
                                postsAdapter = new PostsAdapter(getActivity(), R.layout.post_item, posts);
                                listView.setAdapter(postsAdapter);
                                initFlag = true;
                            } else {
                                for (Post post : posts) {
                                    postsAdapter.add(post);
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    loadingFlag = false;
                }
            }
        });
    }

}

package com.keshavg.reddit;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
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

import static com.keshavg.reddit.Constants.BASE_URL;

public class CommentsActivity extends AppCompatActivity {

    private String url;

    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;

    private ListView commentsList;
    private CommentsAdapter commentsAdapter;

    private List<Comment> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            setTitle(extras.getString("Title"));
            url = BASE_URL + "/api/v1" + extras.getString("Url");
            url = url.substring(0, url.length() - 1);
        }

        Log.d("Comment URL", url);

        comments = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(getApplicationContext(), comments);

        progressBar = (ProgressBar) findViewById(R.id.progressbar_comments);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.comments_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewComments(url);
            }
        });

        commentsList = (ListView) findViewById(R.id.comments_list);
        commentsList.setAdapter(commentsAdapter);

        fetchNewComments(url);
    }

    public void fetchNewComments(String url) {
        this.url = url;
        commentsAdapter.clear();

        try {
            fetchComments(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchComments(String url) throws IOException {

        /**
         * Indicating that the posts are being fetched
         */
//        loadingFlag = true;
//        progressBar.setVisibility(View.VISIBLE);

        comments = new ArrayList<>();

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

                    JSONArray redditComments = jsonObject.getJSONArray("data");

                    for (int idx = 0; idx < redditComments.length(); ++idx) {
                        JSONObject currentComment = redditComments.getJSONObject(idx);
                        Comment comment = new Comment(
                                currentComment.getString("author"),
                                currentComment.getString("body"),
                                currentComment.getInt("created"),
                                currentComment.getJSONObject("replies").getJSONArray("data"),
                                currentComment.getJSONObject("replies").getJSONArray("more"),
                                currentComment.getInt("ups")
                        );

                        comments.add(comment);

                        /**
                         * Logging comments
                         */
                        Log.d("Comment #" + idx + " ", comment.getBody());
                    }

                    /**
                     * Updating the UI on async fetching of posts
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            commentsAdapter.addAll(comments);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    /**
                     * Fetching of posts is completed
                     */
//                    loadingFlag = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            progressBar.setVisibility(View.GONE);
                            swipeContainer.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }
}

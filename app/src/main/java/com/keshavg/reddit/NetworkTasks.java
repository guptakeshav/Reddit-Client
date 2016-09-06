package com.keshavg.reddit;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by keshavgupta on 9/1/16.
 */
public class NetworkTasks {

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public JSONObject fetchJSONFromUrl(String url) throws IOException, JSONException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return new JSONObject(response.body().string());
    }

    public List<Comment> fetchCommentsListFromUrl(String url) throws IOException, JSONException {
        JSONObject jsonObject = fetchJSONFromUrl(url);
        JSONArray redditComments = jsonObject.getJSONArray("data");
        return fetchCommentsList(redditComments);
    }

    public List<Comment> fetchCommentsList(JSONArray redditComments) throws IOException, JSONException {
        List<Comment> comments = new ArrayList<>();
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

        return comments;
    }

    public List<Post> fetchPostsList(JSONArray redditPosts) throws JSONException {
        List<Post> posts = new ArrayList<>();
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

        return posts;
    }
}

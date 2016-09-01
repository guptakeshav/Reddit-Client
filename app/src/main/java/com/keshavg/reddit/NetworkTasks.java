package com.keshavg.reddit;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by keshavgupta on 9/1/16.
 */
public class NetworkTasks {

    OkHttpClient client = new OkHttpClient();

    public String fetchFromUrl(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Comment> fetchCommentsList(String url) {
        List<Comment> comments = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(fetchFromUrl(url));
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return comments;
    }
}

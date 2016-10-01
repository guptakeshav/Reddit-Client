package com.keshavg.reddit.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class PostResponse {
    private class Data {
        private class Response {
            @SerializedName("data") private Post post;

            public Post getPost() {
                return post;
            }
        }

        @SerializedName("children") private List<Response> responses;
        @SerializedName("after") private String afterId;

        public List<Response> getResponses() {
            return responses;
        }

        public String getAfterId() {
            return afterId;
        }
    }

    private Data data;

    public String getAfterId() {
        return data.getAfterId();
    }

    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        for (Data.Response response : data.getResponses()) {
            posts.add(response.getPost());
        }

        return posts;
    }

    public void fixPermalink() {
        for (Post post : getPosts()) {
            post.fixPermalink();
        }
    }

    public void fixImage() {
        for (Post post : getPosts()) {
            post.fixImage();
        }
    }
}
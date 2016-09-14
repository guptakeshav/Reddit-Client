package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class PostResponse {
    @SerializedName("after") private String afterParam;
    @SerializedName("data") private List<Post> posts;

    public String getAfterParam() {
        return afterParam;
    }

    public List<Post> getPosts() {
        return posts;
    }
}

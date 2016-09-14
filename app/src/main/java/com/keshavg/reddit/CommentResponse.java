package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class CommentResponse {
    @SerializedName("more") private List<String> moreIds;
    @SerializedName("data") private List<Comment> comments;

    public List<String> getMoreIds() {
        return moreIds;
    }

    public List<Comment> getComments() {
        return comments;
    }
}

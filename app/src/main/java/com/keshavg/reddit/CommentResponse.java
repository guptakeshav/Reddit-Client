package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Queue;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class CommentResponse {
    @SerializedName("more") private Queue<String> moreIds;
    @SerializedName("data") private List<Comment> comments;

    public Queue<String> getMoreIds() {
        return moreIds;
    }

    public List<Comment> getComments() {
        return comments;
    }
}

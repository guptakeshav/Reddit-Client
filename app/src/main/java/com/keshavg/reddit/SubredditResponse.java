package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class SubredditResponse {
    @SerializedName("after")
    private String afterParam;
    @SerializedName("data")
    private List<Subreddit> subreddits;

    public String getAfterParam() {
        return afterParam;
    }

    public List<Subreddit> getSubreddits() {
        return subreddits;
    }
}

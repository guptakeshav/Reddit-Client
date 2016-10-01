package com.keshavg.reddit.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class SubredditResponse {
    private class Data {
        private class Response {
            @Getter
            @SerializedName("data") private Subreddit subreddit;
        }

        @Getter
        @SerializedName("children") private List<Response> responses;

        @Getter
        @SerializedName("after") private String afterId;
    }

    private Data data;

    public String getAfterId() {
        return data.getAfterId();
    }

    public List<Subreddit> getSubreddits() {
        List<Subreddit> subreddits = new ArrayList<>();
        for (Data.Response response : data.getResponses()) {
            subreddits.add(response.getSubreddit());
        }

        return subreddits;
    }
}

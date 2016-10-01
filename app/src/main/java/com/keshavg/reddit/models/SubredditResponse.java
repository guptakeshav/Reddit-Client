package com.keshavg.reddit.models;

import com.google.gson.annotations.SerializedName;
import com.keshavg.reddit.models.Subreddit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class SubredditResponse {
    private class Data {
        private class Response {
            @SerializedName("data") private Subreddit subreddit;

            public Subreddit getSubreddit() {
                return subreddit;
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

    public List<Subreddit> getSubreddits() {
        List<Subreddit> subreddits = new ArrayList<>();
        for (Data.Response response : data.getResponses()) {
            subreddits.add(response.getSubreddit());
        }

        return subreddits;
    }
}

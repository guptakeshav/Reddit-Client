package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by keshavgupta on 9/13/16.
 */
public class Subreddit {
    @SerializedName("display_name") private String name;
    private String description;

    public Subreddit(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return "r/" + name;
    }

    public String getDescription() {
        return description;
    }
}

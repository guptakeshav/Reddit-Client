package com.keshavg.reddit;

/**
 * Created by keshavgupta on 9/13/16.
 */
public class Subreddit {
    private String name;
    private String description;

    public Subreddit(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

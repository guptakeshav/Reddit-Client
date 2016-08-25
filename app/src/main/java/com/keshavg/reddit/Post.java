package com.keshavg.reddit;

import android.content.Intent;

/**
 * Created by keshav.g on 22/08/16.
 */
public class Post {

    private String author;
    private int created;
    private int numComments;
    private String permalink;
    private int score;
    private String subreddit;
    private String thumbnail;
    private String title;
    private String url;

    public Post(String author, int created, int numComments, String permalink, int score, String subreddit, String thumbnail, String title, String url) {
        this.author = author;
        this.created = created;
        this.numComments = numComments;
        this.permalink = permalink;
        this.score = score;
        this.subreddit = subreddit;
        this.thumbnail = thumbnail;
        this.title = title;
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public int getCreated() {
        return created;
    }

    public String getNumComments() {
        return Integer.toString(numComments) + " comments";
    }

    public String getPermalink() {
        return permalink;
    }

    public String getScore() {
        return Integer.toString(score);
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDetails() {
        return "Posted by " + getAuthor() + " . " + "/r/" + getSubreddit();
    }
}
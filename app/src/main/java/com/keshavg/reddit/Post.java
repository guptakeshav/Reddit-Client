package com.keshavg.reddit;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by keshav.g on 22/08/16.
 */
public class Post {

    private String author;
    private long created;
    @SerializedName("num_comments") private int numComments;
    private String permalink;
    private int score;
    private String subreddit;
    private String thumbnail;
    private String title;
    private String url;

    public Post(String author, long created, int numComments, String permalink, int score, String subreddit, String thumbnail, String title, String url) {
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
        return "Posted by " + author;
    }

    public String getCreated() {
        return DateUtils.getRelativeTimeSpanString(created * 1000L).toString();
    }

    public String getNumComments() {
        return Integer.toString(numComments) + " comments";
    }

    public String getPermalink() {
        return permalink;
    }

    public String getScore() {
        return Integer.toString(score) + " score";
    }

    public String getSubreddit() {
        return "r/" + subreddit;
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
}
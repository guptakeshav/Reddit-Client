package com.keshavg.reddit;

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
        return author;
    }

    public String getCreated() {
        Date date = new Date(created * 1000L);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String dateString = formatter.format(date);

        formatter = new SimpleDateFormat("hh:mm a");
        String time = formatter.format(date);

        return dateString + " " + time;
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
        return "Posted by " + getAuthor() +
                " on " + getCreated() +
                "\n" + "/r/" + getSubreddit();
    }
}
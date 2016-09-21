package com.keshavg.reddit;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by keshav.g on 22/08/16.
 */
public class Post {
    private String name;
    private String author;
    @SerializedName("created_utc") private long created;
    private Boolean likes;
    @SerializedName("num_comments") private int numComments;
    private String permalink;
    private int score;
    private String subreddit;
    private String image;
    private String title;
    private String url;

    public Post(String name,
                String author,
                long created,
                int likes,
                int numComments,
                String permalink,
                int score,
                String subreddit,
                String image,
                String title,
                String url) {
        this.name = name;
        this.author = author;
        this.created = created;

        if (likes == 1) {
            this.likes = true;
        } else if (likes == -1) {
            this.likes = false;
        } else {
            this.likes = null;
        }

        this.numComments = numComments;
        this.permalink = permalink;
        this.score = score;
        this.subreddit = subreddit;
        this.image = image;
        this.title = title;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getPostedBy() {
        return "Posted by " + author;
    }

    public long getCreated() {
        return created;
    }

    public String getRelativeCreatedTimeSpan() {
        return DateUtils.getRelativeTimeSpanString(created * 1000L).toString();
    }

    public int getLikes() {
        if (likes == null) {
            return 0;
        } else if (likes.equals(true)) {
            return 1;
        }

        return -1;
    }

    public void setLikes(int likes) {
        if (likes == 1) {
            this.likes = true;
        } else if (likes == -1) {
            this.likes = false;
        } else {
            this.likes = null;
        }
    }

    public int getNumComments() {
        return numComments;
    }

    public String getCommentsCount() {
        return Integer.toString(numComments) + " comments";
    }

    public String getPermalink() {
        return permalink;
    }

    public void fixPermalink() {
        int idx = permalink.lastIndexOf('/');
        permalink = permalink.substring(0, idx);
    }

    public String getScore() {
        return Integer.toString(score);
    }

    public void updateScore(int delta) {
        this.score += delta;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getFormattedSubreddit() {
        return "r/" + subreddit;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
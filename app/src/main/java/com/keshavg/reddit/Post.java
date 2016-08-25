package com.keshavg.reddit;

/**
 * Created by keshav.g on 22/08/16.
 */
public class Post {
    @Override
    public String toString() {
        return "Post{" +
                "author='" + author + '\'' +
                ", score=" + score +
                ", subreddit='" + subreddit + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    private String author;
    private int score;
    private String subreddit;
    private String thumbnail;
    private String title;
    private String url;

    public Post(String author, int score, String subreddit, String thumbnail, String title, String url) {
        this.author = author;
        this.score = score;
        this.subreddit = subreddit;
        this.thumbnail = thumbnail;
        this.title = title;
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public int getScore() {
        return score;
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
}
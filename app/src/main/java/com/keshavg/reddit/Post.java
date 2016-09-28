package com.keshavg.reddit;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by keshav.g on 22/08/16.
 */
public class Post {
    @Getter
    private String name;

    @Getter
    private String author;

    @Getter
    @SerializedName("created_utc") private long created;

    @SerializedName("saved") private Boolean isSaved;

    @SerializedName("hidden") private Boolean isHidden;

    @SerializedName("likes") private Boolean isLiked;

    @SerializedName("over_18") private Boolean isNsfw;

    @Getter
    @SerializedName("num_comments") private int numComments;

    @Getter
    private String permalink;

    private int score;

    @Getter
    private String subreddit;

    @Getter
    @Setter
    private String image;

    @Getter
    private String title;

    @Getter
    private String url;

    public Post(String name,
                String author,
                long created,
                int isSaved,
                int isHidden,
                int isLiked,
                int isNsfw,
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
        this.isSaved = convertIntToBool(isSaved);
        this.isHidden = convertIntToBool(isHidden);
        this.isLiked = convertIntToBool(isLiked);
        this.isNsfw = convertIntToBool(isNsfw);
        this.numComments = numComments;
        this.permalink = permalink;
        this.score = score;
        this.subreddit = subreddit;
        this.image = image;
        this.title = title;
        this.url = url;
    }

    private int convertBoolToInt(Boolean bool) {
        if (bool == null) {
            return 0;
        } else if (bool.equals(true)) {
            return 1;
        }
        return - 1;
    }

    private Boolean convertIntToBool(int number) {
        if (number == 1) {
            return true;
        } else if (number == -1) {
            return false;
        }

        return null;
    }

    public String getPostedBy() {
        return "Posted by " + author;
    }

    public String getRelativeCreatedTimeSpan() {
        return DateUtils.getRelativeTimeSpanString(created * 1000L).toString();
    }

    public int getIsSaved() {
        return convertBoolToInt(isSaved);
    }

    public void setIsSaved(int saved) {
        isSaved = convertIntToBool(saved);
    }

    public int getIsHidden() {
        return convertBoolToInt(isHidden);
    }

    public void setIsHidden(int hidden) {
        isHidden = convertIntToBool(hidden);
    }

    public int getIsLiked() {
        return convertBoolToInt(isLiked);
    }

    public int getIsNsfw() {
        return convertBoolToInt(isNsfw);
    }

    public void setIsNsfw(int nsfw) {
        isNsfw = convertIntToBool(nsfw);
    }

    public void setIsLiked(int isLiked) {
        this.isLiked = convertIntToBool(isLiked);
    }

    public String getCommentsCount() {
        return Integer.toString(numComments) + " comments";
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

    public String getFormattedSubreddit() {
        return "r/" + subreddit;
    }
}
package com.keshavg.reddit;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Queue;

/**
 * Created by keshav.g on 29/08/16.
 */
public class Comment {
    /**
     * 1
     */
    @SerializedName("children") private Queue<String> moreIds;

    public Queue<String> getMoreIds() {
        return moreIds;
    }

    /**
     * 2
     */
    private String author;
    @SerializedName("body_html") private String body;
    @SerializedName("created_utc") private int created;
    private Boolean likes;
    private String name;
    private CommentResponse replyResponse;
    private int score;

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return "Posted by " + author;
    }

    public String getBody() {
        return body;
    }

    public String getCreated() {
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
        if (likes == 0) {
            this.likes = null;
        } else if (likes == 1) {
            this.likes = true;
        } else {
            this.likes = false;
        }
    }

    public List<Comment> getReplies() {
        if (replyResponse == null) {
            return null;
        }
        return replyResponse.getComments();
    }

    public Queue<String> getMoreReplyIds() {
        if (replyResponse == null) {
            return null;
        }
        return replyResponse.getMoreIds();
    }

    public void setReplyResponse(CommentResponse replyResponse) {
        this.replyResponse = replyResponse;
    }

    public String getScore() {
        return score + " points";
    }

    public void updateScore(int delta) {
        score += delta;
    }
}
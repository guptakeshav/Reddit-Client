package com.keshavg.reddit;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by keshav.g on 29/08/16.
 */
public class Comment implements Serializable {
    /**
     * 1
     */
    @Getter
    @SerializedName("children") private Queue<String> moreIds;

    /**
     * 2
     */

    @Getter
    private String author;

    public String getPostedBy() {
        return "Posted by " + author;
    }

    @Getter
    @SerializedName("body_html") private String body;

    @SerializedName("created_utc") private int created;

    private Boolean likes;

    @Getter
    private String name;

    @Setter
    private CommentResponse replyResponse;

    private int score;

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

    public String getScore() {
        return score + " points";
    }

    public void updateScore(int delta) {
        score += delta;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "moreIds=" + moreIds +
                ", author='" + author + '\'' +
                ", body='" + body + '\'' +
                ", created=" + created +
                ", likes=" + likes +
                ", name='" + name + '\'' +
                ", replyResponse=" + replyResponse +
                ", score=" + score +
                '}';
    }
}
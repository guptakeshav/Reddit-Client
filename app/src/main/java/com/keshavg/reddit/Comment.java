package com.keshavg.reddit;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Queue;

/**
 * Created by keshav.g on 29/08/16.
 */
public class Comment {
    private String author;
    private String body;
    private int created;
    @SerializedName("replies") private CommentResponse replyResponse;
    private int ups;

    public Comment(String author,
                   String body,
                   int created,
                   CommentResponse replyResponse,
                   int ups) {
        this.author = author;
        this.body = body;
        this.created = created;
        this.replyResponse = replyResponse;
        this.ups = ups;
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

    public List<Comment> getReplies() {
        return replyResponse.getComments();
    }

    public Queue<String> getMoreRepliesId() {
        return replyResponse.getMoreIds();
    }

    public String getUps() {
        return Integer.toString(ups) + " points";
    }
}
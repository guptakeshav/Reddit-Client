package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
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
        Date date = new Date(created * 1000L);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String dateString = formatter.format(date);

        formatter = new SimpleDateFormat("hh:mm a");
        String time = formatter.format(date);

        return "on " + dateString + " " + time;
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
package com.keshavg.reddit;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by keshav.g on 29/08/16.
 */
public class Comment {
    private String author;
    private String body;
    private int created;
    private List<Comment> replies;
    private List<String> moreReplies;
    private int ups;

    public Comment(String author,
                   String body,
                   int created,
                   JSONArray replies,
                   JSONArray moreReplies,
                   int ups) {
        this.author = author;
        this.body = body;
        this.created = created;

        try {
            this.replies = new NetworkTasks().fetchCommentsList(replies);
        } catch (IOException ioE) {
            ioE.printStackTrace();
        } catch (JSONException jsonE) {
            jsonE.printStackTrace();
        }

        this.moreReplies = new ArrayList<>();
        for (int idx = 0; idx < moreReplies.length(); ++idx) {
            try {
                this.moreReplies.add(moreReplies.getString(idx));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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
        return replies;
    }

    public List<String> getMoreReplies() {
        return moreReplies;
    }

    public String getUps() {
        return Integer.toString(ups) + " points";
    }
}
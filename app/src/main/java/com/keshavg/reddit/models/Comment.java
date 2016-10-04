package com.keshavg.reddit.models;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;
import com.keshavg.reddit.interfaces.Thing;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by keshav.g on 29/08/16.
 */
public class Comment implements Thing, Serializable {
    /**
     * 1
     */
    @Getter
    @SerializedName("children") private Queue<String> moreIds;

    /**
     * 2
     */

    @Getter
    @SerializedName("link_id") private String postId;

    @Getter
    private String author;

    public String getPostedBy() {
        return "Posted by " + author;
    }

    @Getter
    private String body;

    @Getter
    @Setter
    @SerializedName("body_html") private String htmlBody;

    @Getter
    @SerializedName("created_utc") private int created;

    @Getter
    @SerializedName("likes") private Boolean isLiked;

    @Getter
    @SerializedName("name") private String id;

    @Getter
    @SerializedName("parent_id") private String parentId;

    @Getter
    @Setter
    private CommentResponse replyResponse;

    @Getter
    private int score;

    @Getter
    private String subredditName;

    @Getter
    @SerializedName("link_title") private String postTitle;

    public void setData(Comment comment) {
        this.postId = comment.getPostId();
        this.author = comment.getAuthor();
        this.htmlBody = comment.getHtmlBody();
        this.created = comment.getCreated();
        this.isLiked = comment.getIsLiked();
        this.id = comment.getId();
        this.parentId = comment.getParentId();
        this.replyResponse = comment.getReplyResponse();
        this.score = comment.getScore();
        this.subredditName = comment.getSubredditName();
        this.postTitle = comment.getPostTitle();
    }

    public String getFullCommentLink() {
        String id = postId.substring(3, postId.length());
        return "/r/" + subredditName + "/comments/" + id + "/";
    }

    public String getRelativeTime() {
        return DateUtils.getRelativeTimeSpanString(created * 1000L).toString();
    }

    public int getLikes() {
        if (isLiked == null) {
            return 0;
        } else if (isLiked.equals(true)) {
            return 1;
        }
        return -1;
    }

    public void setIsLiked(int isLiked) {
        if (isLiked == 0) {
            this.isLiked = null;
        } else if (isLiked == 1) {
            this.isLiked = true;
        } else {
            this.isLiked = false;
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

    public String getScoreString() {
        return score + " points";
    }

    public void updateScore(int delta) {
        score += delta;
    }
}
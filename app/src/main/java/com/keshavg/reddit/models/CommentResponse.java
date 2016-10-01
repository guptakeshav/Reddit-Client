package com.keshavg.reddit.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class CommentResponse {
    private class Data {
        private class Response {
            @Getter
            private String kind;

            @Getter
            @SerializedName("data") private Comment comment;
        }

        @Getter
        @SerializedName("children") private List<Response> responses;
    }

    private Data data;

    public Queue<String> getMoreIds() {
        for (Data.Response response : data.getResponses()) {
            if (response.getKind().equals("more")) {
                return response.getComment().getMoreIds();
            }
        }

        return null;
    }

    public List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        for (Data.Response response : data.getResponses()) {
            if (response.getKind().equals("t1")) {
                comments.add(response.getComment());
            }
        }

        return comments;
    }

    public void setReplyResponse(int idx, CommentResponse replyResponse) {
        data.getResponses().get(idx).getComment().setReplyResponse(replyResponse);
    }
}

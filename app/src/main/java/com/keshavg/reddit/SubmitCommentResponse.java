package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/21/16.
 */

public class SubmitCommentResponse {
    private class JsonResponse {
        private class Data {
            private class Response {
                @Getter
                @SerializedName("data") private Comment submittedComment;
            }

            @Getter
            @SerializedName("things") private List<Response> responses;
        }

        @Getter
        private List<List<String>> errors;

        @Getter
        private Data data;
    }

    private JsonResponse json;

    public List<String> getError() {
        if (json.getErrors().size() == 0) {
            return null;
        }
        return json.getErrors().get(0);
    }

    public Comment getSubmittedComment() {
        return json.getData().getResponses().get(0).getSubmittedComment();
    }
}

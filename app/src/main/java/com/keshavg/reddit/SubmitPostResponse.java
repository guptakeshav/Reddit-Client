package com.keshavg.reddit;

import java.util.List;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/26/16.
 */

public class SubmitPostResponse {
    private class Json {
        @Getter
        private List<List<String>> errors;
    }

    private Json json;

    public List<List<String>> getErrors() {
        return json.getErrors();
    }
}

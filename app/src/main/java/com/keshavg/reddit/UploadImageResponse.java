package com.keshavg.reddit;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/27/16.
 */

public class UploadImageResponse {
    private class Data {
        @Getter
        private String link;
    }

    private Data data;

    public String getLink() {
        return data.getLink();
    }
}

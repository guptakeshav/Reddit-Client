package com.keshavg.reddit.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/26/16.
 */

public class CaptchaRepsonse {
    private class Json {
        private class Data {
            @Getter
            @SerializedName("iden") private String identifier;
        }

        @Getter
        private Data data;
    }

    private Json json;

    public String getIden() {
        return json.getData().getIdentifier();
    }
}

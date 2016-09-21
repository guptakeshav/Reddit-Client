package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by keshavgupta on 9/16/16.
 */
public class User {
    private String name;
    @SerializedName("comment_karma") private String commentKarma;
    @SerializedName("created_utc") private Long created;
    @SerializedName("link_karma") private String postKarma;
    @SerializedName("has_verified_email") private Boolean hasVerifiedEmail;

    public String getName() {
        return name;
    }
}
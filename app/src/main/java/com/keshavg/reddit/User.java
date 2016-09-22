package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/16/16.
 */
public class User {
    @Getter
    private String name;

    @Getter
    @SerializedName("comment_karma") private String commentKarma;

    @Getter
    @SerializedName("created_utc") private Long created;

    @Getter
    @SerializedName("link_karma") private String postKarma;

    @Getter
    @SerializedName("has_verified_email") private Boolean hasVerifiedEmail;
}
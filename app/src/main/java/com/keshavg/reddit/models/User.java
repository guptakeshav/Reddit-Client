package com.keshavg.reddit.models;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by keshavgupta on 9/16/16.
 */
public class User {
    @Getter
    @SerializedName("name") private String username;

    @Getter
    @SerializedName("comment_karma") private String commentKarma;

    @SerializedName("created_utc") private Long created;

    @Getter
    @SerializedName("link_karma") private String postKarma;

    @Getter
    @Setter
    @SerializedName("is_friend") private Boolean isFriend;

    public String getCreated() {
        return DateUtils.getRelativeTimeSpanString(created * 1000L).toString();
    }
}
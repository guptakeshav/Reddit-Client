package com.keshavg.reddit.models;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by keshavgupta on 9/13/16.
 */
public class Subreddit {
    @Getter
    private String name;

    @SerializedName("display_name") private String displayName;
    @SerializedName("created_utc") private Long created;

    @Getter
    @SerializedName("description_html") private String description;

    @Getter
    @Setter
    @SerializedName("user_is_subscriber") private Boolean isSubscribed;

    @Getter
    private Long subscribers;

    public String getSubredditName() {
        return "r/" + displayName;
    }

    public String getCreated() {
        return DateUtils.getRelativeTimeSpanString(created * 1000L).toString();
    }
}

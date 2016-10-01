package com.keshavg.reddit.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/23/16.
 */

public class UserResponse {
    @Getter
    @SerializedName("data") private User user;
}

package com.keshavg.reddit.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/16/16.
 */
public class AuthAccessResponse {
    @Getter
    @SerializedName("access_token") private String accessToken;

    @Getter
    @SerializedName("token_type") private String tokenType;

    @Getter
    @SerializedName("expires_in") private String expiresIn;

    @Getter
    private String scope;

    @Getter
    @SerializedName("refresh_token") private String refreshToken;
}

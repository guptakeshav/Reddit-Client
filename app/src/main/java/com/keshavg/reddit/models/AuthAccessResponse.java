package com.keshavg.reddit.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by keshavgupta on 9/16/16.
 */
public class AuthAccessResponse {
    @SerializedName("access_token") private String accessToken;
    @SerializedName("token_type") private String tokenType;
    @SerializedName("expires_in") private String expiresIn;
    private String scope;
    @SerializedName("refresh_token") private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}

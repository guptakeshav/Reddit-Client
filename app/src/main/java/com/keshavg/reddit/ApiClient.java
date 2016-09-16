package com.keshavg.reddit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class ApiClient {
    public static final String BASE_URL = "http://172.16.44.237:65010/";
    private static Retrofit retrofit = null;

    public static final String BASE_URL_OAUTH = "https://oauth.reddit.com/";
    private static Retrofit retrofitOauth = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static Retrofit getOauthClient() {
        if (retrofitOauth == null) {
            retrofitOauth = new Retrofit.Builder()
                    .baseUrl(BASE_URL_OAUTH)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofitOauth;
    }
}
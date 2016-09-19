package com.keshavg.reddit;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class ApiClient {
    public static final String BASE_URL = "http://192.168.0.6:65010/";
    private static Retrofit retrofit = null;

    public static final String BASE_URL_OAUTH = "https://oauth.reddit.com/";
    private static Retrofit retrofitOauth = null;

    public static final String BASE_URL_AUTHENTICATE = "https://www.reddit.com/";
    private static Retrofit retrofitAuthenticate = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static Retrofit getOAuthClient() {
        if (retrofitOauth == null) {
            retrofitOauth = new Retrofit.Builder()
                    .baseUrl(BASE_URL_OAUTH)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofitOauth;
    }

    public static Retrofit getAuthenticateClient(final String CLIENT_ID, final String CLIENT_SECRET) {
        if (retrofitAuthenticate == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .authenticator(new Authenticator() {
                        @Override
                        public Request authenticate(Route route, okhttp3.Response response) throws IOException {
                            String credential = okhttp3.Credentials.basic(CLIENT_ID, CLIENT_SECRET);
                            return response.request().newBuilder()
                                    .header("Authorization", credential)
                                    .build();
                        }
                    }).build();

            retrofitAuthenticate = new Retrofit.Builder()
                    .baseUrl(BASE_URL_AUTHENTICATE)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofitAuthenticate;
    }
}
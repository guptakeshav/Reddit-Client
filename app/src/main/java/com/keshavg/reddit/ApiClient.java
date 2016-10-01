package com.keshavg.reddit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by keshavgupta on 9/14/16.
 */
public class ApiClient {
    public static final String BASE_URL = "https://www.reddit.com/";
    public static final String BASE_URL_OAUTH = "https://oauth.reddit.com/";

    private static Retrofit retrofit = null;
    private static Retrofit retrofitOauth = null;
    private static Retrofit retrofitAuthenticate = null;

    public static class CommentDeserializer implements JsonDeserializer<CommentResponse> {
        @Override
        public CommentResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            CommentResponse commentResponse = new Gson().fromJson(json, CommentResponse.class);

            JsonArray jsonArray = json.getAsJsonObject()
                    .get("data").getAsJsonObject()
                    .get("children").getAsJsonArray();

            for (int idx = 0; idx < jsonArray.size(); ++idx) {
                JsonObject jsonObject = jsonArray
                        .get(idx).getAsJsonObject()
                        .get("data").getAsJsonObject();

                if (jsonObject.has("replies")) {
                    JsonElement jsonElement = jsonObject.get("replies");
                    try {
                        // will reach here if jsonelement is a primitive
                        jsonElement.getAsString();
                    } catch (UnsupportedOperationException e) {
                        CommentResponse reply = getGsonConverter()
                                .fromJson(jsonElement, CommentResponse.class);
                        commentResponse.setReplyResponse(idx, reply);
                    }
                }
            }

            return commentResponse;
        }
    }

    public static Gson getGsonConverter() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(CommentResponse.class, new CommentDeserializer())
                .create();

        return gson;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(getGsonConverter()))
                    .build();
        }

        return retrofit;
    }

    public static Retrofit getOAuthClient() {
        if (retrofitOauth == null) {
            retrofitOauth = new Retrofit.Builder()
                    .baseUrl(BASE_URL_OAUTH)
                    .addConverterFactory(GsonConverterFactory.create(getGsonConverter()))
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
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(getGsonConverter()))
                    .build();
        }

        return retrofitAuthenticate;
    }
}
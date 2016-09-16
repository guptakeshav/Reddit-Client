package com.keshavg.reddit;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by keshavgupta on 9/14/16.
 */
public interface ApiInterface {

    @FormUrlEncoded
    @POST("api/v1/access_token")
    Call<AuthAccessResponse> getAccessToken(@Field("grant_type") String grantType,
                                            @Field("code") String code,
                                            @Field("redirect_uri") String redirectUri);

    @GET("api/v1/subreddits")
    Call<List<String>> getSubredditNames();

    @GET("api/v1/{url}/{sortBy}")
    Call<PostResponse> getPosts(@Path("url") String url,
                                @Path("sortBy") String sortBy);

    @GET("api/v1/{url}/{sortBy}/{after}")
    Call<PostResponse> getPostsAfter(@Path("url") String url,
                                     @Path("sortBy") String sortBy,
                                     @Path("after") String after);

    @GET("api/v1/{url}/{sortBy}")
    Call<CommentResponse> getComments(@Path("url") String url,
                                      @Path("sortBy") String sortBy);

    @GET("api/v1/{url}/{sortBy}/{moreId}")
    Call<CommentResponse> getMoreComments(@Path("url") String url,
                                          @Path("sortBy") String sortBy,
                                          @Path("moreId") String moreId);

    @GET("api/v1/search/subreddits/{query}")
    Call<SubredditResponse> getSubreddits(@Path("query") String query);

    @GET("api/v1/search/subreddits/{query}/{after}")
    Call<SubredditResponse> getSubredditsAfter(@Path("query") String query,
                                               @Path("after") String after);

    @GET("api/v1/me")
    Call<User> getUsername(@Header("Authorization") String authorization);
}
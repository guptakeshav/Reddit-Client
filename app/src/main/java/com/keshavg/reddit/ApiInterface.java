package com.keshavg.reddit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by keshavgupta on 9/14/16.
 */
public interface ApiInterface {
    @GET("api/v1/subreddits")
    Call<List<String>> getSubredditNames();

    @GET("{urlSortBy}")
    Call<PostResponse> getPosts(@Path("urlSortBy") String urlSortBy,
                                @Query("after") String afterParam);

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

    /**
     * --------------------------------------------------------------------------------------------
     * Beginning of OAuth 2.0 requests
     * --------------------------------------------------------------------------------------------
     */

    @FormUrlEncoded
    @POST("api/v1/access_token")
    Call<AuthAccessResponse> getAccessToken(@Field("grant_type") String grantType,
                                            @Field("code") String code,
                                            @Field("redirect_uri") String redirectUri);

    @FormUrlEncoded
    @POST("api/v1/revoke_token")
    Call<Void> revokeToken(@Field("token") String token,
                           @Field("token_type_hint") String tokenType);

    @FormUrlEncoded
    @POST("api/v1/access_token")
    Call<AuthAccessResponse> refreshToken(@Field("grant_type") String grantType,
                                          @Field("refresh_token") String refreshToken);

    @GET("api/v1/me")
    Call<User> getUsername(@Header("Authorization") String authorization);

    @GET("oauth/{urlSortBy}")
    Call<PostResponse> getOAuthPosts(@Header("Authorization") String authorization,
                                     @Path("urlSortBy") String urlSortBy,
                                     @Query("after") String afterParam);

    @FormUrlEncoded
    @POST("api/vote")
    Call<Void> votePost(@Header("Authorization") String authorization,
                        @Field("id") String name,
                        @Field("dir") int vote);
}
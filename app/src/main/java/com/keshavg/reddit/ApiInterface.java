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
    @GET("subreddits.json")
    Call<SubredditResponse> getSubredditNames();

    @GET("{urlSortBy}/.json")
    Call<PostResponse> getPosts(@Path("urlSortBy") String urlSortBy,
                                @Query("after") String afterParam);

    @GET("{url}.json")
    Call<List<CommentResponse>> getComments(@Path("url") String url,
                                            @Query("sort") String sortByParam,
                                            @Query("raw_json") int value);

    @GET("{url}/{moreId}.json")
    Call<List<CommentResponse>> getMoreComments(@Path("url") String url,
                                                @Path("moreId") String moreId,
                                                @Query("sort") String sortByParam,
                                                @Query("raw_json") int value);

    @GET("subreddits/search.json")
    Call<SubredditResponse> getSubreddits(@Query("q") String query,
                                          @Query("after") String afterParam,
                                          @Query("raw_json") int value);

    // TODO: search in subreddits
    @GET("search.json")
    Call<PostResponse> searchPosts(@Query("q") String searchQuery,
                                   @Query("sort") String sortByParam,
                                   @Query("after") String afterParam);

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

    @GET("{urlSortBy}")
    Call<PostResponse> getOAuthPosts(@Header("Authorization") String authorization,
                                     @Path("urlSortBy") String urlSortBy,
                                     @Query("after") String afterParam);

    @FormUrlEncoded
    @POST("api/vote")
    Call<Void> votePost(@Header("Authorization") String authorization,
                        @Field("id") String name,
                        @Field("dir") int vote);

    // TODO: search in subreddits
    @GET("search")
    Call<PostResponse> searchOAuthPosts(@Header("Authorization") String authorization,
                                        @Query("q") String searchQuery,
                                        @Query("sort") String sortByParam,
                                        @Query("after") String afterParam);

    @GET("{url}")
    Call<List<CommentResponse>> getOAuthComments(@Header("Authorization") String authorization,
                                                 @Path("url") String url,
                                                 @Query("sort") String sortBy,
                                                 @Query("raw_json") int value);

    @GET("{url}/{moreId}")
    Call<List<CommentResponse>> getMoreOAuthComments(@Header("Authorization") String authorization,
                                                     @Path("url") String url,
                                                     @Path("moreId") String moreId,
                                                     @Query("sort") String sortBy,
                                                     @Query("raw_json") int value);

    @FormUrlEncoded
    @POST("api/comment")
    Call<Void> submitComment(@Header("Authorization") String authorization,
                             @Field("api_type") String apiType,
                             @Field("text") String text,
                             @Field("thing_id") String parentId);
}
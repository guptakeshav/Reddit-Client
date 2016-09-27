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
    @GET("user/{username}/about.json")
    Call<UserResponse> getUserOverview(@Path("username") String username);

    @GET("subreddits.json")
    Call<SubredditResponse> getSubredditNames();

    @GET("{urlSortBy}.json")
    Call<PostResponse> getPosts(@Path("urlSortBy") String urlSortBy,
                                @Query("after") String afterParam);

    @GET("{url}/{moreId}.json")
    Call<List<CommentResponse>> getComments(@Path("url") String url,
                                            @Path("moreId") String moreId,
                                            @Query("sort") String sortByParam,
                                            @Query("raw_json") int value);

    @GET("{url}/{moreId}.json")
    Call<CommentResponse> getProfileComments(@Path("url") String url,
                                             @Path("moreId") String moreId,
                                             @Query("sort") String sortBy,
                                             @Query("raw_json") int value);

    @GET("subreddits/search.json")
    Call<SubredditResponse> searchSubreddits(@Query("q") String query,
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

    @GET("subreddits/mine")
    Call<SubredditResponse> getOAuthSubreddits();

    @GET("api/v1/me")
    Call<User> getUsername(@Header("Authorization") String authorization);

    @FormUrlEncoded
    @POST("api/new_captcha")
    Call<CaptchaRepsonse> getNewCaptcha(@Header("Authorization") String authorization,
                                        @Field("api_type") String apiType);

    @FormUrlEncoded
    @POST("api/submit")
    Call<SubmitPostResponse> createPost(@Header("Authorization") String authorization,
                                        @Field("api_type") String apiType,
                                        @Field("kind") String kind,
                                        @Field("title") String title,
                                        @Field("sr") String subreddit,
                                        @Field("iden") String captchaIden,
                                        @Field("captcha") String captcha,
                                        @Field("text") String text,
                                        @Field("url") String url,
                                        @Field("sendreplies") Boolean sendReplies);

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

    @GET("{url}/{moreId}")
    Call<List<CommentResponse>> getOAuthComments(@Header("Authorization") String authorization,
                                                 @Path("url") String url,
                                                 @Path("moreId") String moreId,
                                                 @Query("sort") String sortBy,
                                                 @Query("raw_json") int value);

    @GET("{url}/{moreId}")
    Call<CommentResponse> getOAuthProfileComments(@Header("Authorization") String authorization,
                                                  @Path("url") String url,
                                                  @Path("moreId") String moreId,
                                                  @Query("sort") String sortBy,
                                                  @Query("raw_json") int value);

    @FormUrlEncoded
    @POST("api/comment")
    Call<SubmitCommentResponse> submitComment(@Header("Authorization") String authorization,
                                              @Query("raw_json") int value,
                                              @Field("api_type") String apiType,
                                              @Field("text") String text,
                                              @Field("thing_id") String parentId);

    @FormUrlEncoded
    @POST("api/editusertext")
    Call<SubmitCommentResponse> editComment(@Header("Authorization") String authorization,
                                            @Query("raw_json") int value,
                                            @Field("api_type") String apiType,
                                            @Field("text") String text,
                                            @Field("thing_id") String id);

    @FormUrlEncoded
    @POST("api/save")
    Call<Void> saveThing(@Header("Authorization") String authorization,
                         @Field("id") String id);

    @FormUrlEncoded
    @POST("api/unsave")
    Call<Void> unsaveThing(@Header("Authorization") String authorization,
                           @Field("id") String id);

    @FormUrlEncoded
    @POST("api/hide")
    Call<Void> hideThing(@Header("Authorization") String authorization,
                         @Field("id") String id);

    @FormUrlEncoded
    @POST("api/unhide")
    Call<Void> unhideThing(@Header("Authorization") String authorization,
                           @Field("id") String id);

    @FormUrlEncoded
    @POST("api/del")
    Call<Void> deleteThing(@Header("Authorization") String authorization,
                           @Field("id") String id);

    @GET("subreddits/search")
    Call<SubredditResponse> searchOAuthSubreddits(@Header("Authorization") String authorization,
                                                  @Query("q") String searchQuery,
                                                  @Query("after") String afterId,
                                                  @Query("raw_json") int value);

    @FormUrlEncoded
    @POST("api/subscribe")
    Call<Void> subscribeSubreddit(@Header("Authorization") String authorization,
                                  @Field("action") String action,
                                  @Field("sr") String subredditName);
}
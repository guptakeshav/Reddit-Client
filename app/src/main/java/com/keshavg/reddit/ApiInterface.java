package com.keshavg.reddit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by keshavgupta on 9/14/16.
 */
public interface ApiInterface {

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
}
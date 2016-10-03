package com.keshavg.reddit.services;

import android.content.Context;
import android.widget.Toast;

import com.keshavg.reddit.R;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.interfaces.PerformFunction;
import com.keshavg.reddit.models.Comment;
import com.keshavg.reddit.models.CommentResponse;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshavgupta on 10/3/16.
 */

public class CommentService {
    @Getter
    @Setter
    private CommentResponse commentResponse;

    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void fetchCommentsForPosts(final Context context,
                                      String url,
                                      String moreId,
                                      String sortByParam,
                                      final PerformFunction onSuccess,
                                      final PerformFunction onFailure) {

        RedditApiInterface apiService;
        Call<List<CommentResponse>> callMore;

        if (AuthSharedPrefHelper.isLoggedIn()) {
            apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
            callMore = apiService.getOAuthComments(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    url,
                    moreId,
                    sortByParam,
                    1
            );
        } else {
            apiService = RedditApiClient.getClient().create(RedditApiInterface.class);
            callMore = apiService.getComments(url, moreId, sortByParam, 1);
        }

        callMore.enqueue(new Callback<List<CommentResponse>>() {
            @Override
            public void onResponse(Call<List<CommentResponse>> call, Response<List<CommentResponse>> response) {
                if (response.isSuccessful()) {
                    setCommentResponse(response.body().get(1));
                    onSuccess.execute();
                } else {
                    showToast(context, "Load More - " + response.message());
                    onFailure.execute();
                }
            }

            @Override
            public void onFailure(Call<List<CommentResponse>> call, Throwable t) {
                showToast(context, context.getString(R.string.error_server_connect));
                onFailure.execute();
            }
        });
    }

    public void fetchCommentsForProfile(final Context context,
                                        String url,
                                        String moreId,
                                        final PerformFunction onSuccess,
                                        final PerformFunction onFailure) {

        RedditApiInterface apiService;
        Call<CommentResponse> call;

        if (AuthSharedPrefHelper.isLoggedIn()) {
            apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
            call = apiService.getOAuthProfileComments(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    url,
                    moreId,
                    "",
                    1
            );
        } else {
            apiService = RedditApiClient.getClient().create(RedditApiInterface.class);
            call = apiService.getProfileComments(url, moreId, "", 1);
        }

        call.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                if (response.isSuccessful()) {
                    setCommentResponse(response.body());
                    onSuccess.execute();
                } else {
                    showToast(context, "Comments - " + response.message());
                    onFailure.execute();
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                showToast(context, context.getString(R.string.error_server_connect));
                onFailure.execute();
            }
        });
    }

    public static void deleteComment(final Context context, String id, final PerformFunction onDelete) {
        RedditApiInterface apiClient = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
        Call<Void> call = apiClient.deleteThing(
                "bearer " + AuthSharedPrefHelper.getAccessToken(),
                id
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    onDelete.execute();
                } else {
                    showToast(context, "Deleting - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(context, context.getString(R.string.error_server_connect));
            }
        });
    }

    public static void voteComment(final Context context, final Comment comment, final PerformFunction onVoteUnsuccess) {
        RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
        Call<Void> call = apiService.votePost(
                "bearer " + AuthSharedPrefHelper.getAccessToken(),
                comment.getName(),
                comment.getLikeInt()
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    showToast(context, "Comments Voting - " + response.message());
                    onVoteUnsuccess.execute();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(context, context.getString(R.string.error_server_connect));
                onVoteUnsuccess.execute();
            }
        });
    }
}
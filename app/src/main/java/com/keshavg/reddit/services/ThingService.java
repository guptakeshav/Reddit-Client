package com.keshavg.reddit.services;

import android.content.Context;
import android.widget.Toast;

import com.keshavg.reddit.R;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.interfaces.PerformFunction;
import com.keshavg.reddit.interfaces.Thing;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshavgupta on 10/4/16.
 */

public class ThingService {
    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void deleteThing(final Context context, String id, final PerformFunction onDelete) {
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

    public static void voteThing(final Context context, final Thing thing, final PerformFunction onVoteUnsuccess) {
        RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
        Call<Void> call = apiService.voteThing(
                "bearer " + AuthSharedPrefHelper.getAccessToken(),
                thing.getId(),
                thing.getLikes()
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    if (response.code() == 400) {
                        showToast(context, context.getString(R.string.error_archive_post));
                    } else {
                        showToast(context, "Voting - " + response.message());
                    }

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

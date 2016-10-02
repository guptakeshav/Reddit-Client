package com.keshavg.reddit.services;

import android.content.Context;
import android.widget.Toast;

import com.keshavg.reddit.R;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.interfaces.PerformFunction;
import com.keshavg.reddit.models.User;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by keshavgupta on 10/2/16.
 */

public class UserInfoService {
    public static final String USERNAME = "USERNAME";

    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void fetchUsername(final Context context, final PerformFunction showUserName) {
        Retrofit retrofit = RedditApiClient.getOAuthClient();
        RedditApiInterface apiService = retrofit.create(RedditApiInterface.class);

        Call<User> call = apiService.getUsername("bearer " + AuthSharedPrefHelper.getAccessToken());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    AuthSharedPrefHelper.add(USERNAME, response.body().getUsername());

                    showUserName.execute();
                } else {
                    showToast(context, "Username - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showToast(context, context.getString(R.string.error_server_connect));
            }
        });
    }
}

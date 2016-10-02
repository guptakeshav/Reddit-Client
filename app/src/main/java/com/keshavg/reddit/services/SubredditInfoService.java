package com.keshavg.reddit.services;

import android.content.Context;
import android.widget.Toast;

import com.keshavg.reddit.R;
import com.keshavg.reddit.interfaces.PerformFunction;
import com.keshavg.reddit.models.Subreddit;
import com.keshavg.reddit.models.SubredditResponse;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshavgupta on 10/2/16.
 */

public class SubredditInfoService {
    @Getter
    @Setter
    public List<Subreddit> subreddits;

    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void fetchSubscriptions(final Context context, final PerformFunction onSubscriptionsFetched) {
        RedditApiInterface apiService = RedditApiClient.getClient().create(RedditApiInterface.class);
        Call<SubredditResponse> call = apiService.getSubredditNames();
        call.enqueue(new Callback<SubredditResponse>() {
            @Override
            public void onResponse(Call<SubredditResponse> call, Response<SubredditResponse> response) {
                if (response.isSuccessful()) {
                    setSubreddits(response.body().getSubreddits());
                    onSubscriptionsFetched.execute();
                } else {
                    showToast(context, "Subreddits - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SubredditResponse> call, Throwable t) {
                showToast(context, context.getString(R.string.error_server_connect));
            }
        });
    }
}
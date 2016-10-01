package com.keshavg.reddit.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.keshavg.reddit.R;
import com.keshavg.reddit.activities.WebViewActivity;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.interfaces.PerformFunction;
import com.keshavg.reddit.models.AuthAccessResponse;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshavgupta on 10/2/16.
 */

public class LoginService {
    private final static String OAUTH_URL = "https://www.reddit.com/api/v1/authorize.compact";
    private final static String CLIENT_ID = "v438H_DJQuG0oQ";
    private final static String CLIENT_SECRET = "";
    private final static String RESPONSE_TYPE = "code";
    private final static String STATE = "TEST";
    private final static String REDIRECT_URI = "http://localhost";
    private final static String DURATION = "permanent";
    private final static String SCOPE = "modothers,modposts,report,subscribe,livemanage,history,creddits," +
            "modflair,modwiki,vote,wikiread,mysubreddits,flair,modself,submit,modcontributors," +
            "account,modtraffic,read,modlog,modmail,edit,modconfig,save,privatemessages,identity,wikiedit";

    public final static String AUTH_CODE = "AUTH_CODE";
    public final static int AUTH_CODE_REQUEST = 200;

    private Context context;
    public LoginService(Context context) {
        this.context = context;
    }

    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void fetchAuthCode(Activity activity) {
        String url = OAUTH_URL
                + "?client_id=" + CLIENT_ID
                + "&response_type=" + RESPONSE_TYPE
                + "&state=" + STATE
                + "&redirect_uri=" + REDIRECT_URI
                + "&duration=" + DURATION
                + "&scope=" + SCOPE;

        Intent i = new Intent(activity, WebViewActivity.class);
        i.putExtra("URL", url);
        activity.startActivityForResult(i, AUTH_CODE_REQUEST);
    }

    public static void fetchAccessToken(final Context context, String authCode, final PerformFunction onLogin) {
        RedditApiInterface apiService = RedditApiClient.getAuthenticateClient(CLIENT_ID, CLIENT_SECRET)
                .create(RedditApiInterface.class);

        Call<AuthAccessResponse> call = apiService.getAccessToken(
                "authorization_code",
                authCode,
                REDIRECT_URI
        );
        call.enqueue(new Callback<AuthAccessResponse>() {
            @Override
            public void onResponse(Call<AuthAccessResponse> call, final Response<AuthAccessResponse> response) {

                if (response.isSuccessful()) {
                    AuthSharedPrefHelper.add("ACCESS_TOKEN", response.body().getAccessToken());
                    Long expiresIn = (Integer.parseInt(response.body().getExpiresIn()) - 60) * 1000L;
                    AuthSharedPrefHelper.add("EXPIRES_IN", Long.toString(expiresIn));
                    AuthSharedPrefHelper.add("REFRESH_TOKEN", response.body().getRefreshToken());

//                    TODO: how to refresh token?
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            refreshToken();
//                        }
//                    }, expiresIn);

                    onLogin.execute();
                } else {
                    showToast(context, response.message());
                }
            }

            @Override
            public void onFailure(Call<AuthAccessResponse> call, Throwable t) {
                showToast(context, context.getString(R.string.server_error));
            }
        });
    }

    public static void logoutUser(final Context context, final PerformFunction onLogout) {
        RedditApiInterface apiService = RedditApiClient.getAuthenticateClient(CLIENT_ID, CLIENT_SECRET)
                .create(RedditApiInterface.class);
        Call<Void> call = apiService.revokeToken(AuthSharedPrefHelper.getAccessToken(), "access_token");

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    AuthSharedPrefHelper.clearPreferences();

                    onLogout.execute();
                } else {
                    showToast(context, "Logout - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(context, context.getString(R.string.server_error));
            }
        });
    }

    public void refreshToken(final Context context) {
        final String refreshToken = AuthSharedPrefHelper.getString("REFRESH_TOKEN");
        final Long expiresIn = Long.parseLong(AuthSharedPrefHelper.getString("EXPIRES_IN"));

        RedditApiInterface apiService = RedditApiClient.getAuthenticateClient(CLIENT_ID, CLIENT_SECRET)
                .create(RedditApiInterface.class);
        Call<AuthAccessResponse> call = apiService.refreshToken("refresh_token", refreshToken);
        call.enqueue(new Callback<AuthAccessResponse>() {
            @Override
            public void onResponse(Call<AuthAccessResponse> call, Response<AuthAccessResponse> response) {
                if (response.isSuccessful()) {
                    AuthSharedPrefHelper.add("ACCESS_TOKEN", response.body().getAccessToken());
                } else {
                    showToast(context, "Refreshing Token - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<AuthAccessResponse> call, Throwable t) {
                showToast(context, context.getString(R.string.server_error));
            }
        });
    }
}
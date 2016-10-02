package com.keshavg.reddit.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.keshavg.reddit.services.UserInfoService;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by keshavgupta on 10/2/16.
 */

public class AuthSharedPrefHelper {
    private static SharedPreferences authPref;

    public static void createInstance(Context context) {
        authPref = context.getSharedPreferences("AuthPref", MODE_PRIVATE);
    }

    public static Boolean has(String key) {
        return authPref.contains(key);
    }

    public static String getString(String key) {
        return authPref.getString(key, "");
    }

    public static void add(String key, String value) {
        SharedPreferences.Editor editor = authPref.edit();
        editor.putString(key, value).commit();
    }

    public static void remove(String key) {
        SharedPreferences.Editor editor = authPref.edit();
        editor.remove(key).commit();
    }

    public static void clearPreferences() {
        SharedPreferences.Editor editor = authPref.edit();
        editor.clear().commit();
    }

    public static String getAccessToken() {
        String accessToken = authPref.getString("ACCESS_TOKEN", "");
        Log.d("AccessToken", accessToken);
        return accessToken;
    }

    public static Boolean isLoggedIn() {
        if (authPref.contains("ACCESS_TOKEN")) {
            return true;
        }
        return false;
    }

    public static String getUsername() {
        return authPref.getString(UserInfoService.USERNAME, "");
    }

    public static Boolean isSessionExpired() {
        return false; // TODO
    }
}

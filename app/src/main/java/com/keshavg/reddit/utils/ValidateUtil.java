package com.keshavg.reddit.utils;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.services.LoginService;

/**
 * Created by keshavgupta on 10/2/16.
 */

public class ValidateUtil {
    private static final String LOGIN_ERROR = "You must be logged in to perform this function.";

    public static Boolean loginValidation(View view, final Activity activity) {
        if (!AuthSharedPrefHelper.isLoggedIn()) {
            Snackbar.make(view, LOGIN_ERROR, Snackbar.LENGTH_LONG)
                    .setAction("Login", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LoginService.fetchAuthCode(activity);
                        }
                    })
                    .show();

            return false;
        }

        return true;
    }
}
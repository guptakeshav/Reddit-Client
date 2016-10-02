package com.keshavg.reddit.utils;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.keshavg.reddit.R;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.services.LoginService;

/**
 * Created by keshavgupta on 10/2/16.
 */

public class ValidateUtil {
    public static Boolean loginValidation(View view, final Activity activity) {
        if (!AuthSharedPrefHelper.isLoggedIn()) {
            Snackbar.make(view, activity.getString(R.string.error_login), Snackbar.LENGTH_LONG)
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
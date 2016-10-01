package com.keshavg.reddit.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by keshavgupta on 9/30/16.
 */

public class DeviceUtil {
    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}

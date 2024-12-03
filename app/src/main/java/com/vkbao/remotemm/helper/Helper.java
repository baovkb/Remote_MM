package com.vkbao.remotemm.helper;


import android.content.Context;
import android.util.TypedValue;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Helper {
    public static int convertDpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    public static String unixTime2Date(String unixTime) {
        long unix = Long.parseLong(unixTime);
        Date date = new Date(unix);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyy");

        return simpleDateFormat.format(date);
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        return displayMetrics.heightPixels;
    }
}

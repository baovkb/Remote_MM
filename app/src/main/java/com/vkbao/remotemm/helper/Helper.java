package com.vkbao.remotemm.helper;

import android.content.Context;
import android.util.TypedValue;

public class Helper {
    public static int convertDpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }
}

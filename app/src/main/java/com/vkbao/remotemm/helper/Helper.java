package com.vkbao.remotemm.helper;


import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.vkbao.remotemm.model.FileInfo;
import com.vkbao.remotemm.model.SuccessResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Response;

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

    public static interface VoidCallback {
        public void invoke();
    }

    public static <T> CompletableFuture<ApiState<T>> apiHandler(ApiHandler<T> apiHandler) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<ApiState<T>> resultFuture = new CompletableFuture<>();

            try {
                Response<T> response = apiHandler.apiCall().execute();

                if (response.isSuccessful() && response.body() != null) {
                    resultFuture.complete(ApiState.success(response.body()));
                } else {
                    String errorBody = response.errorBody().string();
                    JSONObject jsonObject = new JSONObject(errorBody);
                    String errorMessage = jsonObject.optString("message", "Unknown error");
                    resultFuture.complete(ApiState.error(errorMessage));
                }
            } catch (IOException | JSONException e) {
                resultFuture.complete(ApiState.error(e.getMessage()));
            }

            return resultFuture;
        }).thenCompose(result -> result);
    }

    public static interface ApiHandler<T> {
        public Call<T> apiCall();
    }
}

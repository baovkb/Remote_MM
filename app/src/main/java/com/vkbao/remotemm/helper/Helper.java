package com.vkbao.remotemm.helper;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.vkbao.remotemm.model.FileInfo;
import com.vkbao.remotemm.model.SuccessResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    public static String getRealPathFromURI(Context context, Uri uri) {
        String result = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            if (idx != -1) result = cursor.getString(idx);
            cursor.close();
        }
        return result != null ? result : uri.getPath();
    }

    public static List<MultipartBody.Part> prepareFileParts(Context context, List<Uri> fileUris) {
        List<MultipartBody.Part> parts = new ArrayList<>();

        for (Uri uri : fileUris) {
            File file = new File(getRealPathFromURI(context, uri));
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            parts.add(part);
        }

        return parts;
    }
}

package com.vkbao.remotemm.helper;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CustomInterceptor implements Interceptor {
    private  String baseUrl = "http://localhost";
    private int port = 9091;
    private static CustomInterceptor instance;

    private CustomInterceptor() {}

    public void setBaseUrl(String newBaseUrl) {
        baseUrl = newBaseUrl;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static synchronized CustomInterceptor getInstance() {
        if (instance == null) {
            instance = new CustomInterceptor();
        }

        return instance;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        HttpUrl oldUrl = originalRequest.url();

        HttpUrl.Builder newUrlBuilder = HttpUrl.parse(baseUrl).newBuilder()
                .encodedPath(oldUrl.encodedPath())
                .encodedQuery(oldUrl.encodedQuery());

        if (oldUrl.port() != -1) {
            newUrlBuilder.port(port);
        }


        Request newRequest = originalRequest.newBuilder()
                .url(newUrlBuilder.build())
                .method(originalRequest.method(), originalRequest.body())
                .headers(originalRequest.headers())
                .build();

        Log.d("request", newRequest.url().toString());

        return chain.proceed(newRequest);
    }
}

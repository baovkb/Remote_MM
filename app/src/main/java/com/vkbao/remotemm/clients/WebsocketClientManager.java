package com.vkbao.remotemm.clients;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class WebsocketClientManager {
    private WebSocket webSocket;
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    private WebsocketClientManager() {
    }

    public static WebsocketClientManager getInstance() {
        return WebsocketClientHelper.INSTANCE;
    }

    private static class WebsocketClientHelper {
        private static final WebsocketClientManager INSTANCE = new WebsocketClientManager();
    }

    public void initWebsocket(String url) {
        if (webSocket != null) {
            webSocket.close(1000, "reconnecting");
        }

        OkHttpClient client = new OkHttpClient();
        Request request = null;
        try {
            request = new Request.Builder().url(url).build();
        } catch (Exception e) {
            messageLiveData.postValue("error_url");
            return;
        }

        messageLiveData.postValue("connecting");

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                messageLiveData.postValue(text);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                messageLiveData.postValue("failure");
                Log.d("test", t.toString());
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                messageLiveData.postValue("opened");
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                if (code == 1000) return;

                messageLiveData.postValue("disconnected");
            }
        });
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public boolean sendMessage(String message) {
        if (webSocket == null) return false;

        webSocket.send(message);
        return true;
    }
}

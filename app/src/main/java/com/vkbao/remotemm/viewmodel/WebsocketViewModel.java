package com.vkbao.remotemm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vkbao.remotemm.clients.WebsocketClientManager;

import okhttp3.WebSocket;

public class WebsocketViewModel extends AndroidViewModel {
    private WebsocketClientManager websocketClientManager;
    private LiveData<String> messageLiveData;

    public WebsocketViewModel(@NonNull Application application) {
        super(application);
        websocketClientManager = WebsocketClientManager.getInstance();
        messageLiveData = websocketClientManager.getMessageLiveData();
    }

    public void connect(String url) {
        websocketClientManager.initWebsocket(url);
    }

    public boolean sendMessage(String message) {
        return websocketClientManager.sendMessage(message);
    }

    public LiveData<String> getMessageLiveData() {
        return websocketClientManager.getMessageLiveData();
    }
}

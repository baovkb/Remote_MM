package com.vkbao.remotemm.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vkbao.remotemm.clients.WebsocketClientManager;
import com.vkbao.remotemm.helper.CustomJson;
import com.vkbao.remotemm.model.ModuleModel;
import com.vkbao.remotemm.model.ModulesByPageResponse;
import com.vkbao.remotemm.model.SystemInfoResponse;
import com.vkbao.remotemm.model.VolumeModel;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebsocketViewModel extends AndroidViewModel {
    private WebsocketClientManager websocketClientManager;

    private MediatorLiveData<CONNECTION_STATE> connectionStateLiveData;
    private MutableLiveData<List<ModuleModel>> allModuleLiveData;
    private MutableLiveData<ModulesByPageResponse> modulesByPageLiveData;
    private MutableLiveData<VolumeModel> volumeLiveData;
    private MutableLiveData<List<String>> backupFileLiveData;

    private final Gson gson;

    public WebsocketViewModel(@NonNull Application application) {
        super(application);
        websocketClientManager = WebsocketClientManager.getInstance();

        allModuleLiveData = new MutableLiveData<>();
        modulesByPageLiveData = new MutableLiveData<>();
        volumeLiveData = new MutableLiveData<>();
        connectionStateLiveData = new MediatorLiveData<>();
        backupFileLiveData = new MutableLiveData<>();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Map.class, new CustomJson());
        gson = gsonBuilder.create();

        connectionStateLiveData.addSource(websocketClientManager.getMessageLiveData(), message -> {
            switch (message) {
                case "error_url":
                    connectionStateLiveData.postValue(CONNECTION_STATE.ERROR_URL);
                    break;
                case "connecting":
                    connectionStateLiveData.postValue(CONNECTION_STATE.CONNECTING);
                    break;
                case "failure":
                case "disconnected":
                    connectionStateLiveData.postValue(CONNECTION_STATE.DISCONNECTED);
                    break;
                case "opened":
                    connectionStateLiveData.postValue(CONNECTION_STATE.UNAUTHENTICATED);
                    break;
                default:
                    Map<String, Object> map;
                    String action = null;
                    try {
                        map = gson.fromJson(message, new TypeToken<Map<String, Object>>() {
                        }.getType());
                        action = map.get("action").toString();
                        Log.d("action ws", action);
                    } catch (Exception ignored) {
                        Log.d("error map ws msg", ignored.getMessage());
                        break;
                    }

                    switch (action) {
                        case "authenticated":
                            connectionStateLiveData.postValue(CONNECTION_STATE.CONNECTED);
                            break;
                        case "authentication failure":
                            connectionStateLiveData.postValue(CONNECTION_STATE.AUTHENTICATION_FAILED);
                            break;
                        case "system info":
                            try {
                                SystemInfoResponse sysInfo = gson.fromJson(message, SystemInfoResponse.class);

                                //update volume
                                volumeLiveData.postValue(new VolumeModel(
                                        sysInfo.getSpeaker(),
                                        sysInfo.getRecorder()));

                                //update all modules
                                allModuleLiveData.postValue(sysInfo.getAllModules());

                                //update modules by page
                                modulesByPageLiveData.postValue(sysInfo.getModulesByPage());
                            } catch (Exception ignored) {
                            }

                            break;
                        case "volume":
                            try {
                                String dataJson = gson.toJson(map.get("data"));
                                volumeLiveData.postValue(gson.fromJson(dataJson, VolumeModel.class));
                            } catch (Exception ignored) {
                            }
                            break;
                        case "all modules":
                            try {
                                String dataJson = gson.toJson(map.get("data"));
                                Type moduleListType = new TypeToken<List<ModuleModel>>() {
                                }.getType();
                                allModuleLiveData.postValue(gson.fromJson(dataJson, moduleListType));
                            } catch (Exception ignored) {
                            }
                            break;
                        case "modules by page":
                            try {
                                String dataJson = gson.toJson(map.get("data"));
                                modulesByPageLiveData.postValue(gson.fromJson(dataJson, ModulesByPageResponse.class));
                            } catch (Exception ignored) {}
                            break;
                        case "backup files":
                            try {
                                String dataJson = gson.toJson(map.get("data"));
                                Type fileListType = new TypeToken<List<String>>() {
                                }.getType();
                                backupFileLiveData.postValue(gson.fromJson(dataJson, fileListType));
                            } catch (Exception ignored) {
                            }
                            break;
                        default:
                    }
            }
        });
    }

    public void connect(String url) {
        websocketClientManager.initWebsocket(url);
    }

    public boolean sendMessage(String message) {
        return websocketClientManager.sendMessage(message);
    }

    public MediatorLiveData<CONNECTION_STATE> getConnectionStateLiveData() {
        return connectionStateLiveData;
    }

    public MutableLiveData<List<ModuleModel>> getAllModuleLiveData() {
        return allModuleLiveData;
    }

    public MutableLiveData<ModulesByPageResponse> getModulesByPageLiveData() {
        return modulesByPageLiveData;
    }

    public LiveData<List<String>> getBackupFileLiveData() {
        return backupFileLiveData;
    }

    public MutableLiveData<VolumeModel> getVolumeLiveData() {
        return volumeLiveData;
    }

    public enum CONNECTION_STATE {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR_URL,
        UNAUTHENTICATED,
        AUTHENTICATION_FAILED
    }
}

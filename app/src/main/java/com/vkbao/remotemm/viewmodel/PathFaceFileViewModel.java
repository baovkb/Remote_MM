package com.vkbao.remotemm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PathFaceFileViewModel extends AndroidViewModel {
    private MutableLiveData<String> path = new MutableLiveData<>("");

    public PathFaceFileViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<String> getPath () {
        return path;
    }

    public void updatePath(String newPath) {
        path.postValue(newPath);
    }
}

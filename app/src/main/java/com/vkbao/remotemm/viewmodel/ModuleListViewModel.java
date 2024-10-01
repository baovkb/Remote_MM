package com.vkbao.remotemm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vkbao.remotemm.model.ModuleModel;

import java.util.List;

public class ModuleListViewModel extends AndroidViewModel {
    private MutableLiveData<List<ModuleModel>> moduleLiveData;

    public ModuleListViewModel(@NonNull Application application) {
        super(application);
        moduleLiveData = new MutableLiveData<>();
    }

    public LiveData<List<ModuleModel>> getModuleLiveData() {
        return moduleLiveData;
    }

    public void setModuleList(List<ModuleModel> moduleList) {
        moduleLiveData.postValue(moduleList);
    }
}

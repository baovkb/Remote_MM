package com.vkbao.remotemm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vkbao.remotemm.helper.ApiState;
import com.vkbao.remotemm.model.FileInfo;
import com.vkbao.remotemm.model.SuccessResponse;
import com.vkbao.remotemm.model.request.DeleteFileRequest;
import com.vkbao.remotemm.repository.FileRepository;

import java.util.List;
import java.util.function.Consumer;

public class FileViewModel extends AndroidViewModel {
    private final FileRepository fileRepository;
    private final MutableLiveData<ApiState<SuccessResponse<List<FileInfo>>>> fileListLiveData = new MutableLiveData<>();

    public LiveData<ApiState<SuccessResponse<String>>> getDeleteFileLiveData() {
        return deleteFileLiveData;
    }

    private final MutableLiveData<ApiState<SuccessResponse<String>>> deleteFileLiveData = new MutableLiveData<>();

    public FileViewModel(@NonNull Application application) {
        super(application);

        fileRepository = new FileRepository();
    }

    public LiveData<ApiState<SuccessResponse<List<FileInfo>>>> getFileListLiveData() {
        return fileListLiveData;
    }

    public void getList(String path) {
        fileRepository.getList(path).thenAccept(fileListLiveData::postValue);
    }

    public void deleteFile(String path) {
        DeleteFileRequest request = new DeleteFileRequest(path);
        fileRepository.delete(request).thenAccept(deleteFileLiveData::postValue);
    }
}

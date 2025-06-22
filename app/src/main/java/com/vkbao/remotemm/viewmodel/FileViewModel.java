package com.vkbao.remotemm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.Api;
import com.vkbao.remotemm.helper.ApiState;
import com.vkbao.remotemm.model.FileInfo;
import com.vkbao.remotemm.model.SuccessResponse;
import com.vkbao.remotemm.model.request.CreateFolderRequest;
import com.vkbao.remotemm.model.request.DeleteFileRequest;
import com.vkbao.remotemm.model.request.UploadImageRequest;
import com.vkbao.remotemm.repository.FileRepository;

import java.util.List;
import java.util.function.Consumer;

import okhttp3.MultipartBody;

public class FileViewModel extends AndroidViewModel {
    private final FileRepository fileRepository;
    private final MutableLiveData<ApiState<SuccessResponse<List<FileInfo>>>> fileListLiveData = new MutableLiveData<>();
    private final MutableLiveData<ApiState<SuccessResponse<String>>> deleteFileLiveData = new MutableLiveData<>();
    private final MutableLiveData<ApiState<SuccessResponse<String>>> createFolderLiveData = new MutableLiveData<>();
    private final MutableLiveData<ApiState<SuccessResponse<String>>> uploadImagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<ApiState<SuccessResponse<String>>> encodeLiveData = new MutableLiveData<>();

    public FileViewModel(@NonNull Application application) {
        super(application);

        fileRepository = new FileRepository();
    }

    public LiveData<ApiState<SuccessResponse<List<FileInfo>>>> getFileListLiveData() {
        return fileListLiveData;
    }

    public LiveData<ApiState<SuccessResponse<String>>> getDeleteFileLiveData() {
        return deleteFileLiveData;
    }

    public LiveData<ApiState<SuccessResponse<String>>> getCreateFolderLiveData() {
        return createFolderLiveData;
    }

    public LiveData<ApiState<SuccessResponse<String>>> getUploadImagesLiveData() {
        return uploadImagesLiveData;
    }

    public LiveData<ApiState<SuccessResponse<String>>> getEncodeLiveData() {
        return encodeLiveData;
    }

    public void getList(String path) {
        fileRepository.getList(path).thenAccept(fileListLiveData::postValue);
    }

    public void deleteFile(String path) {
        DeleteFileRequest request = new DeleteFileRequest(path);
        fileRepository.delete(request).thenAccept(deleteFileLiveData::postValue);
    }

    public void createFolder(String path) {
        CreateFolderRequest request = new CreateFolderRequest(path);
        fileRepository.createFolder(request).thenAccept(createFolderLiveData::postValue);
    }

    public void uploadImages(List<MultipartBody.Part> fileParts, String path) {
        UploadImageRequest request = new UploadImageRequest(fileParts, path);
        fileRepository.uploadImages(request).thenAccept(uploadImagesLiveData::postValue);
    }

    public void encodeFaces() {
        fileRepository.encodeFaces().thenAccept(encodeLiveData::postValue);
    }
}

package com.vkbao.remotemm.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.Api;
import com.vkbao.remotemm.clients.ApiClient;
import com.vkbao.remotemm.helper.ApiState;
import com.vkbao.remotemm.helper.Helper;
import com.vkbao.remotemm.model.FileInfo;
import com.vkbao.remotemm.model.SuccessResponse;
import com.vkbao.remotemm.model.request.CreateFolderRequest;
import com.vkbao.remotemm.model.request.DeleteFileRequest;
import com.vkbao.remotemm.model.request.UploadImageRequest;
import com.vkbao.remotemm.service.ApiService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class FileRepository {
    private final ApiService apiService;

    public FileRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public CompletableFuture<ApiState<SuccessResponse<String>>> createFolder(CreateFolderRequest request) {
        return Helper.apiHandler(() -> apiService.createFolder(request));
    }

    public CompletableFuture<ApiState<SuccessResponse<List<FileInfo>>>> getList(String path) {
        return Helper.apiHandler(() -> apiService.getFile(path));
    }

    public CompletableFuture<ApiState<SuccessResponse<String>>> delete(DeleteFileRequest request) {
        return Helper.apiHandler(() -> apiService.delete(request));
    }

    public CompletableFuture<ApiState<SuccessResponse<String>>> uploadImages(UploadImageRequest request) {
        return Helper.apiHandler(() -> apiService.uploadFile(request.getFileParts(), request.getPathBody()));
    }

    public CompletableFuture<ApiState<SuccessResponse<String>>> encodeFaces() {
        return Helper.apiHandler(() -> apiService.encodeFaces());
    }
}

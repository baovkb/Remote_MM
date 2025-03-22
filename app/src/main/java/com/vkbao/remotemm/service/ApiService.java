package com.vkbao.remotemm.service;

import com.vkbao.remotemm.model.FileInfo;
import com.vkbao.remotemm.model.SuccessResponse;
import com.vkbao.remotemm.model.request.CreateFolderRequest;
import com.vkbao.remotemm.model.request.DeleteFileRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @GET("list")
    Call<SuccessResponse<List<FileInfo>>> getFile(@Query("path") String path);

    @POST("mkdir")
    Call<SuccessResponse<String>> createFolder(@Body CreateFolderRequest request);

    @HTTP(method = "DELETE", path = "delete", hasBody = true)
    Call<SuccessResponse<String>> delete(@Body DeleteFileRequest request);

    @Multipart
    @POST("upload")
    Call<SuccessResponse<String>> uploadFile(
            @Part List<MultipartBody.Part> files,
            @Part("path")RequestBody path
    );

    @POST("encode")
    Call<SuccessResponse<String>> encodeFaces();
}

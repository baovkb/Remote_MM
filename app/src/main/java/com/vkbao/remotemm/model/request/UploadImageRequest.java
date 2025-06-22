package com.vkbao.remotemm.model.request;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UploadImageRequest {
    private List<MultipartBody.Part> fileParts;
    private RequestBody pathBody;

    public UploadImageRequest(List<MultipartBody.Part> fileParts, String path) {
        this.fileParts = fileParts;
        this.pathBody = RequestBody.create(path, MediaType.get("text/plain"));
    }

    public List<MultipartBody.Part> getFileParts() {
        return fileParts;
    }

    public void setFileParts(List<MultipartBody.Part> fileParts) {
        this.fileParts = fileParts;
    }

    public RequestBody getPathBody() {
        return pathBody;
    }

    public void setPathBody(RequestBody pathBody) {
        this.pathBody = pathBody;
    }
}

package com.vkbao.remotemm.model.request;

public class DeleteFileRequest {
    private String path;

    public DeleteFileRequest(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

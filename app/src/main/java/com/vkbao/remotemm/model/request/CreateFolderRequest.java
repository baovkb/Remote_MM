package com.vkbao.remotemm.model.request;

public class CreateFolderRequest {
    private String path;

    public CreateFolderRequest(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

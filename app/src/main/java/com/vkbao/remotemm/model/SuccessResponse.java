package com.vkbao.remotemm.model;

public class SuccessResponse<T> {
    private T data;

    public SuccessResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

package com.vkbao.remotemm.helper;

public class ApiState<T> {
    public enum Status {SUCCESS, ERROR }

    public final Status status;
    public final T data;
    public final String message;

    private ApiState(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiState<T> success(T data) {
        return new ApiState<>(Status.SUCCESS, data, null);
    }

    public static <T> ApiState<T> error(String message) {
        return new ApiState<>(Status.ERROR, null, message);
    }
}

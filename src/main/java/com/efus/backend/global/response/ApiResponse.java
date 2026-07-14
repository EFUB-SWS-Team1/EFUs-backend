package com.efus.backend.global.response;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "요청이 성공했습니다.");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static ApiResponse<Void> successWithoutData() {
        return new ApiResponse<>(true, null, "요청이 성공했습니다.");
    }

    public static ApiResponse<Void> successWithMessage(String message) {
        return new ApiResponse<>(true, null, message);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
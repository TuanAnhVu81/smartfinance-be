package com.smartfinance.exception;

import lombok.Builder;

// Generic API response wrapper for all endpoints
// Structure: { success, code, message, data }
@Builder
public record ApiResponse<T>(
        boolean success,
        int code,
        String message,
        T data
) {
    // Factory method for successful response with data
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }

    // Factory method for successful response with custom message
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    // Factory method for error response using ErrorCode enum
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .data(null)
                .build();
    }
}

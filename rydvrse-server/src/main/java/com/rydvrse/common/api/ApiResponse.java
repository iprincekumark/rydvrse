package com.rydvrse.common.api;

import com.rydvrse.common.util.RequestIdHolder;

public record ApiResponse<T>(T data, ApiMeta meta) {
    public static <T> ApiResponse<T> of(T data, String requestId) {
        return new ApiResponse<>(data, ApiMeta.now(requestId == null ? RequestIdHolder.get() : requestId));
    }
}

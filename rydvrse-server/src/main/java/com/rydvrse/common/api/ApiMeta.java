package com.rydvrse.common.api;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiMeta(
        String requestId,
        OffsetDateTime timestamp,
        String apiVersion,
        Map<String, Object> pagination
) {
    public static ApiMeta now(String requestId) {
        return new ApiMeta(requestId, OffsetDateTime.now(), "v1", null);
    }

    public static ApiMeta now(String requestId, Map<String, Object> pagination) {
        return new ApiMeta(requestId, OffsetDateTime.now(), "v1", pagination);
    }
}

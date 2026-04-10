package com.rydvrse.common.api;

import java.util.List;

public record ApiErrorBody(
        String code,
        String message,
        String type,
        boolean retryable,
        List<ApiErrorDetail> details
) {
}

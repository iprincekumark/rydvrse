package com.rydvrse.common.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final String type;
    private final boolean retryable;
    private final Map<String, String> details;

    public ApiException(HttpStatus status, ErrorCode errorCode, String message, String type, boolean retryable, Map<String, String> details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.type = type;
        this.retryable = retryable;
        this.details = details == null ? Map.of() : details;
    }

    public static ApiException badRequest(ErrorCode errorCode, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, errorCode, message, "validation_error", false, Map.of());
    }

    public static ApiException unauthorized(ErrorCode errorCode, String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, errorCode, message, "authentication_error", false, Map.of());
    }

    public static ApiException forbidden(ErrorCode errorCode, String message) {
        return new ApiException(HttpStatus.FORBIDDEN, errorCode, message, "authorization_error", false, Map.of());
    }

    public static ApiException notFound(ErrorCode errorCode, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, errorCode, message, "not_found", false, Map.of());
    }

    public static ApiException conflict(ErrorCode errorCode, String message) {
        return new ApiException(HttpStatus.CONFLICT, errorCode, message, "conflict", false, Map.of());
    }

    public static ApiException unprocessable(ErrorCode errorCode, String message) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, errorCode, message, "business_rule_violation", false, Map.of());
    }

    public static ApiException unavailable(ErrorCode errorCode, String message) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, errorCode, message, "dependency_failure", true, Map.of());
    }

    public HttpStatus status() {
        return status;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public String type() {
        return type;
    }

    public boolean retryable() {
        return retryable;
    }

    public Map<String, String> details() {
        return details;
    }
}

package com.rydvrse.shared.exception;

/**
 * Base exception for all RYDVRSE business exceptions.
 */
public class RydvrseException extends RuntimeException {

    private final String errorCode;

    public RydvrseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RydvrseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

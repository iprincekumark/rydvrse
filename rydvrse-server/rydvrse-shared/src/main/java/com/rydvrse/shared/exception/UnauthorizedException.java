package com.rydvrse.shared.exception;

public class UnauthorizedException extends RydvrseException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}

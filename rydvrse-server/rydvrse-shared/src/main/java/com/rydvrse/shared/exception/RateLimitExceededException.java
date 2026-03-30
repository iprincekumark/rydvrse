package com.rydvrse.shared.exception;

public class RateLimitExceededException extends RydvrseException {
    public RateLimitExceededException(String endpoint) {
        super("RATE_LIMIT_EXCEEDED",
              String.format("Rate limit exceeded for %s. Please try again later.", endpoint));
    }
}

package com.rydvrse.shared.exception;

public class PaymentFailedException extends RydvrseException {
    public PaymentFailedException(String message) {
        super("PAYMENT_FAILED", message);
    }

    public PaymentFailedException(String message, Throwable cause) {
        super("PAYMENT_FAILED", message);
        initCause(cause);
    }
}

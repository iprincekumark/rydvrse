package com.rydvrse.shared.exception;

public class BusinessRuleException extends RydvrseException {
    public BusinessRuleException(String message) {
        super("BUSINESS_RULE_VIOLATION", message);
    }

    public BusinessRuleException(String errorCode, String message) {
        super(errorCode, message);
    }
}

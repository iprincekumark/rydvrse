package com.rydvrse.shared.exception;

public class DuplicateResourceException extends RydvrseException {
    public DuplicateResourceException(String resource, String field, String value) {
        super("DUPLICATE_" + resource.toUpperCase(),
              String.format("%s with %s '%s' already exists", resource, field, value));
    }
}

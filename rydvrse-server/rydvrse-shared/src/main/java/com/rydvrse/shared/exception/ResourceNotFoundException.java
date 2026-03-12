package com.rydvrse.shared.exception;

public class ResourceNotFoundException extends RydvrseException {
    public ResourceNotFoundException(String resource, String identifier) {
        super("NOT_FOUND", resource + " not found with identifier: " + identifier);
    }
}

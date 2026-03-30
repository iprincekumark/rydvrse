package com.rydvrse.shared.exception;

public class InvalidStateTransitionException extends RydvrseException {
    public InvalidStateTransitionException(String entity, String currentState, String targetState) {
        super("INVALID_STATE_TRANSITION",
              String.format("Cannot transition %s from %s to %s", entity, currentState, targetState));
    }
}

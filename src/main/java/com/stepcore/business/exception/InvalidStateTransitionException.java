package com.stepcore.business.exception;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(final String message) {
        super(message);
    }
}

package com.stepcore.business.exception;

public class ChecklistViolationException extends RuntimeException {
    public ChecklistViolationException(final String message) {
        super(message);
    }
}

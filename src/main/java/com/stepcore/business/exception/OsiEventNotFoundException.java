package com.stepcore.business.exception;

public class OsiEventNotFoundException extends RuntimeException {
    public OsiEventNotFoundException(final Long id) {
        super("OSI event not found: " + id);
    }
}

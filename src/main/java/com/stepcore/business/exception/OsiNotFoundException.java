package com.stepcore.business.exception;

public class OsiNotFoundException extends RuntimeException {
    public OsiNotFoundException(final Long id) {
        super("OSI not found: " + id);
    }
}

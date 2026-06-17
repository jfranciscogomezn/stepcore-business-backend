package com.stepcore.business.exception;

public class OsiAlreadyClosedException extends RuntimeException {
    public OsiAlreadyClosedException(final Long osiId) {
        super("OSI " + osiId + " is already CLOSED and cannot be modified");
    }
}

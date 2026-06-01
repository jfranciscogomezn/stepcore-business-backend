package com.stepcore.business.exception;

public class TimeRecordNotFoundException extends RuntimeException {

    public TimeRecordNotFoundException(final Long id) {
        super("Time record not found: " + id);
    }
}

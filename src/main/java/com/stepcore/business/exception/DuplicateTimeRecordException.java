package com.stepcore.business.exception;

public class DuplicateTimeRecordException extends RuntimeException {

    public DuplicateTimeRecordException(final String message) {
        super(message);
    }
}

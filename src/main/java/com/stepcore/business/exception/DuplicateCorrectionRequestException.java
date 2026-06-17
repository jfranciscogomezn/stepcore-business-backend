package com.stepcore.business.exception;

public class DuplicateCorrectionRequestException extends RuntimeException {

    public DuplicateCorrectionRequestException(final Long timeRecordId) {
        super("A pending correction request already exists for time record: " + timeRecordId);
    }
}

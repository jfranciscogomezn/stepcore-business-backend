package com.stepcore.business.exception;

public class CorrectionRequestNotPendingException extends RuntimeException {

    public CorrectionRequestNotPendingException(final Long requestId) {
        super("Correction request " + requestId + " is not in PENDING status");
    }
}

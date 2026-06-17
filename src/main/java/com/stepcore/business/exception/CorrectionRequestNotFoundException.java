package com.stepcore.business.exception;

public class CorrectionRequestNotFoundException extends RuntimeException {

    public CorrectionRequestNotFoundException(final Long id) {
        super("Correction request not found: " + id);
    }
}

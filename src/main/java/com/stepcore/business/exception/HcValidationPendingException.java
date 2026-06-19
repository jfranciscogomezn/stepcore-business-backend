package com.stepcore.business.exception;

public class HcValidationPendingException extends RuntimeException {
    public HcValidationPendingException() {
        super("HC_VALIDATION_PENDING: documentary validation is still pending for this assignment.");
    }
}

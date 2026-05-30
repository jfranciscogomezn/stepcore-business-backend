package com.stepcore.business.exception;

public class PayrollConfigNotFoundException extends RuntimeException {

    public PayrollConfigNotFoundException(final int year) {
        super("Payroll configuration not found for year " + year);
    }
}

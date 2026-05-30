package com.stepcore.business.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(final Long id) {
        super("Employee not found: " + id);
    }
}

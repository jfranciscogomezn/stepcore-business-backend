package com.stepcore.business.exception;

public class DuplicateEmployeeEmailException extends RuntimeException {

    public DuplicateEmployeeEmailException(final String email) {
        super("Employee with email already exists: " + email);
    }
}

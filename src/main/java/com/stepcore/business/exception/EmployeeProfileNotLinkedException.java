package com.stepcore.business.exception;

public class EmployeeProfileNotLinkedException extends RuntimeException {

    public EmployeeProfileNotLinkedException(final String email) {
        super("No employee profile is linked to user: " + email);
    }
}

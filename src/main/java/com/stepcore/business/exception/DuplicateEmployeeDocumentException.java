package com.stepcore.business.exception;

public class DuplicateEmployeeDocumentException extends RuntimeException {

    public DuplicateEmployeeDocumentException(final String idNumber) {
        super("Employee with document number already exists: " + idNumber);
    }
}

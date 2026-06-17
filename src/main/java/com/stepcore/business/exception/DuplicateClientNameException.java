package com.stepcore.business.exception;

public class DuplicateClientNameException extends RuntimeException {
    public DuplicateClientNameException(final String name) {
        super("A client with name '" + name + "' already exists for this tenant");
    }
}

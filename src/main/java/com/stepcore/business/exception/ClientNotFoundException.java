package com.stepcore.business.exception;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(final Long id) {
        super("Client not found: " + id);
    }
}

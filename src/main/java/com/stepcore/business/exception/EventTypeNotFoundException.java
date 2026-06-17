package com.stepcore.business.exception;

public class EventTypeNotFoundException extends RuntimeException {
    public EventTypeNotFoundException(final Long id) {
        super("Event type not found: " + id);
    }
}

package com.stepcore.business.exception;

public class HolidayNotFoundException extends RuntimeException {

    public HolidayNotFoundException(final Long id) {
        super("Holiday not found: " + id);
    }
}

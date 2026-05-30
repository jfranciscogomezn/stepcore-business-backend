package com.stepcore.business.exception;

import java.time.LocalDate;

public class DuplicateHolidayException extends RuntimeException {

    public DuplicateHolidayException(final LocalDate date) {
        super("Holiday already exists for date " + date);
    }
}

package com.stepcore.business.exception;

import java.time.LocalDate;
import java.util.List;

public class InvalidReportPeriodException extends RuntimeException {

    public InvalidReportPeriodException(final String message) {
        super(message);
    }
}

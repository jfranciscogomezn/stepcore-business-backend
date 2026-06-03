package com.stepcore.business.exception;

import java.time.LocalDate;
import java.util.List;

public class IncompleteReportException extends RuntimeException {

    private final List<LocalDate> incompleteDates;

    public IncompleteReportException(final List<LocalDate> incompleteDates) {
        super("Report period contains incomplete time records");
        this.incompleteDates = List.copyOf(incompleteDates);
    }

    public List<LocalDate> getIncompleteDates() {
        return incompleteDates;
    }
}

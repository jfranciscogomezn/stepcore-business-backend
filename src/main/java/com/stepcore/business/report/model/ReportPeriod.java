package com.stepcore.business.report.model;

import java.time.LocalDate;

public record ReportPeriod(LocalDate start, LocalDate end) {

    public static final int MAX_SPAN_DAYS = 366;
}

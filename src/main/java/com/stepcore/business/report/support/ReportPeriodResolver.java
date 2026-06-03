package com.stepcore.business.report.support;

import com.stepcore.business.exception.InvalidReportPeriodException;
import com.stepcore.business.report.model.ReportPeriod;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
public class ReportPeriodResolver {

    public ReportPeriod resolve(
            final LocalDate date,
            final YearMonth month,
            final LocalDate weekStart,
            final LocalDate startDate,
            final LocalDate endDate) {
        int modes = 0;
        if (date != null) {
            modes++;
        }
        if (month != null) {
            modes++;
        }
        if (weekStart != null) {
            modes++;
        }
        if (startDate != null || endDate != null) {
            modes++;
        }
        if (modes != 1) {
            throw new InvalidReportPeriodException("Exactly one report filter mode must be provided");
        }

        if (date != null) {
            return new ReportPeriod(date, date);
        }
        if (month != null) {
            return new ReportPeriod(month.atDay(1), month.atEndOfMonth());
        }
        if (weekStart != null) {
            return new ReportPeriod(weekStart, weekStart.plusDays(6));
        }
        if (startDate == null || endDate == null) {
            throw new InvalidReportPeriodException("Both startDate and endDate are required for custom range");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidReportPeriodException("startDate must be on or before endDate");
        }
        if (startDate.plusDays(ReportPeriod.MAX_SPAN_DAYS).isBefore(endDate)) {
            throw new InvalidReportPeriodException("Date range cannot exceed " + ReportPeriod.MAX_SPAN_DAYS + " days");
        }
        return new ReportPeriod(startDate, endDate);
    }
}

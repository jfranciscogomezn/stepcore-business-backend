package com.stepcore.business.i18n;

import com.stepcore.business.exception.DuplicateEmployeeDocumentException;
import com.stepcore.business.exception.DuplicateEmployeeEmailException;
import com.stepcore.business.exception.DuplicateHolidayException;
import com.stepcore.business.exception.DuplicateTimeRecordException;
import com.stepcore.business.exception.EmployeeNotFoundException;
import com.stepcore.business.exception.EmployeeProfileNotLinkedException;
import com.stepcore.business.exception.HolidayNotFoundException;
import com.stepcore.business.exception.IncompleteReportException;
import com.stepcore.business.exception.InvalidReportPeriodException;
import com.stepcore.business.exception.InvalidTimeRecordOperationException;
import com.stepcore.business.exception.PayrollConfigNotFoundException;
import com.stepcore.business.exception.TimeRecordNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ApiMessageService {

    private static final Pattern EMPLOYEE_ID = Pattern.compile("Employee not found: (\\d+)");
    private static final Pattern TIME_RECORD_ID = Pattern.compile("Time record not found: (\\d+)");
    private static final Pattern HOLIDAY_ID = Pattern.compile("Holiday not found: (\\d+)");
    private static final Pattern PAYROLL_YEAR = Pattern.compile("Payroll configuration not found for year (\\d+)");
    private static final Pattern HOLIDAY_DATE = Pattern.compile("Holiday already exists for date (.+)");
    private static final Pattern RANGE_DAYS = Pattern.compile("Date range cannot exceed (\\d+) days");

    private static final Map<String, String> TIME_OP_KEYS = Map.ofEntries(
            Map.entry("Employee already clocked in for today", "error.time.alreadyClockedIn"),
            Map.entry("No clock-in record found for today", "error.time.noClockInToday"),
            Map.entry("Time record is already closed for today", "error.time.alreadyClosedToday"),
            Map.entry("Only closed records can be reopened", "error.time.onlyClosedReopen"),
            Map.entry("Only incomplete records can be resolved", "error.time.onlyIncompleteResolve"),
            Map.entry("At least one of clock-in or clock-out must be provided", "error.time.clockRequired"),
            Map.entry("Clock-out is required to close a corrected record", "error.time.clockOutRequired"),
            Map.entry("A time record already exists for this employee and date", "error.time.duplicateDate"),
            Map.entry("Clock-out must be after clock-in", "error.time.clockOutAfterIn")
    );

    private static final Map<String, String> REPORT_PERIOD_KEYS = Map.of(
            "Exactly one report filter mode must be provided", "error.report.filterModeRequired",
            "Both startDate and endDate are required for custom range", "error.report.rangeDatesRequired",
            "startDate must be on or before endDate", "error.report.startBeforeEnd"
    );

    private final MessageSource messageSource;

    public String get(final String code, final Object... args) {
        final Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, code, locale);
    }

    public String resolveKey(final String code, final Object... args) {
        return get(code, args);
    }

    public String resolve(final Throwable throwable, final String fallback) {
        if (throwable instanceof IncompleteReportException) {
            return get("error.report.incompleteRecords");
        }
        if (throwable instanceof EmployeeNotFoundException) {
            return get("error.employeeNotFound", extractGroup(EMPLOYEE_ID, throwable.getMessage()));
        }
        if (throwable instanceof TimeRecordNotFoundException) {
            return get("error.timeRecordNotFound", extractGroup(TIME_RECORD_ID, throwable.getMessage()));
        }
        if (throwable instanceof EmployeeProfileNotLinkedException ex) {
            return get("error.employeeProfileNotLinked", ex.getMessage().replace("No employee profile is linked to user: ", ""));
        }
        if (throwable instanceof DuplicateEmployeeEmailException ex) {
            return get("error.duplicateEmployeeEmail", ex.getMessage().replace("Employee with email already exists: ", ""));
        }
        if (throwable instanceof DuplicateEmployeeDocumentException ex) {
            return get("error.duplicateEmployeeDocument", ex.getMessage().replace("Employee with document number already exists: ", ""));
        }
        if (throwable instanceof DuplicateHolidayException) {
            return get("error.duplicateHoliday", extractGroup(HOLIDAY_DATE, throwable.getMessage()));
        }
        if (throwable instanceof HolidayNotFoundException) {
            return get("error.holidayNotFound", extractGroup(HOLIDAY_ID, throwable.getMessage()));
        }
        if (throwable instanceof PayrollConfigNotFoundException) {
            return get("error.payrollConfigNotFound", extractGroup(PAYROLL_YEAR, throwable.getMessage()));
        }
        if (throwable instanceof InvalidTimeRecordOperationException ex) {
            final String key = TIME_OP_KEYS.get(ex.getMessage());
            if (key != null) {
                return get(key);
            }
        }
        if (throwable instanceof DuplicateTimeRecordException ex) {
            final String key = TIME_OP_KEYS.get(ex.getMessage());
            if (key != null) {
                return get(key);
            }
        }
        if (throwable instanceof InvalidReportPeriodException ex) {
            if (REPORT_PERIOD_KEYS.containsKey(ex.getMessage())) {
                return get(REPORT_PERIOD_KEYS.get(ex.getMessage()));
            }
            final Matcher rangeMatcher = RANGE_DAYS.matcher(ex.getMessage());
            if (rangeMatcher.matches()) {
                return get("error.report.rangeTooLarge", rangeMatcher.group(1));
            }
        }
        if (throwable instanceof AuthenticationException) {
            return get("error.unauthorized");
        }
        if (fallback != null && fallback.contains("insufficient permissions")) {
            return get("error.accessDenied");
        }
        if (fallback != null && fallback.contains("API endpoint not found")) {
            return get("error.endpointNotFound");
        }
        return fallback != null ? fallback : get("error.unexpected");
    }

    private String extractGroup(final Pattern pattern, final String message) {
        final Matcher matcher = pattern.matcher(message);
        return matcher.matches() ? matcher.group(1) : message;
    }
}

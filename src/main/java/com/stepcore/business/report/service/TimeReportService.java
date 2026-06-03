package com.stepcore.business.report.service;

import com.stepcore.business.earnings.EarningsCalculationService;
import com.stepcore.business.earnings.model.EarningsResult;
import com.stepcore.business.employee.domain.model.Employee;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.exception.EmployeeNotFoundException;
import com.stepcore.business.exception.EmployeeProfileNotLinkedException;
import com.stepcore.business.exception.IncompleteReportException;
import com.stepcore.business.payroll.domain.model.PayrollConfig;
import com.stepcore.business.payroll.repository.HolidayRepository;
import com.stepcore.business.payroll.repository.PayrollConfigRepository;
import com.stepcore.business.report.dto.TimeReportRecordResponse;
import com.stepcore.business.report.dto.TimeReportResponse;
import com.stepcore.business.report.model.ReportPeriod;
import com.stepcore.business.report.support.ReportPeriodResolver;
import com.stepcore.business.time.domain.model.TimeRecord;
import com.stepcore.business.time.domain.model.TimeRecordStatus;
import com.stepcore.business.time.repository.TimeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeReportService {

    private final EmployeeRepository employeeRepository;
    private final TimeRecordRepository timeRecordRepository;
    private final PayrollConfigRepository payrollConfigRepository;
    private final HolidayRepository holidayRepository;
    private final EarningsCalculationService earningsCalculationService;
    private final ReportPeriodResolver reportPeriodResolver;
    private final TimeReportExcelExporter excelExporter;

    public TimeReportResponse buildReport(
            final String userEmail,
            final boolean isAdmin,
            final Long employeeId,
            final LocalDate date,
            final YearMonth month,
            final LocalDate weekStart,
            final LocalDate startDate,
            final LocalDate endDate,
            final boolean capped) {
        final Employee employee = resolveEmployee(userEmail, isAdmin, employeeId);
        final ReportPeriod period = reportPeriodResolver.resolve(date, month, weekStart, startDate, endDate);
        final List<TimeRecord> records = timeRecordRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(
                        employee.getId(), period.start(), period.end());

        final List<LocalDate> incompleteDates = records.stream()
                .filter(record -> record.getStatus() == TimeRecordStatus.INCOMPLETE)
                .map(TimeRecord::getWorkDate)
                .sorted()
                .toList();
        if (!incompleteDates.isEmpty()) {
            throw new IncompleteReportException(incompleteDates);
        }

        final List<TimeReportRecordResponse> rows = new ArrayList<>();
        BigDecimal totalUncapped = BigDecimal.ZERO;
        BigDecimal totalCapped = BigDecimal.ZERO;

        for (final TimeRecord record : records) {
            if (record.getStatus() != TimeRecordStatus.CLOSED || record.getClockOut() == null) {
                continue;
            }
            final PayrollConfig config = payrollConfigRepository.findByYear(record.getWorkDate().getYear())
                    .orElseThrow(() -> new IllegalStateException(
                            "Payroll config missing for year " + record.getWorkDate().getYear()));
            final boolean sundayOrHoliday = isSundayOrHoliday(record.getWorkDate());
            final EarningsResult earnings = earningsCalculationService.calculateDailyEarnings(
                    record.getClockIn(),
                    record.getClockOut(),
                    record.getWorkDate(),
                    employee.getMonthlySalary(),
                    config,
                    sundayOrHoliday);

            totalUncapped = totalUncapped.add(earnings.uncappedEarnings());
            totalCapped = totalCapped.add(earnings.cappedEarnings());

            rows.add(new TimeReportRecordResponse(
                    record.getId(),
                    record.getWorkDate(),
                    record.getClockIn(),
                    record.getClockOut(),
                    record.getStatus().name(),
                    record.isCorrected(),
                    record.getCorrectionReason(),
                    earnings.classifiedMinutes(),
                    earnings.cappedMinutes(),
                    earnings.uncappedEarnings(),
                    earnings.cappedEarnings(),
                    earnings.highlightLevel()));
        }

        rows.sort(Comparator.comparing(TimeReportRecordResponse::workDate));

        return new TimeReportResponse(
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                capped,
                rows,
                totalUncapped,
                totalCapped);
    }

    public byte[] exportExcel(final TimeReportResponse report) {
        return excelExporter.export(report);
    }

    private Employee resolveEmployee(final String userEmail, final boolean isAdmin, final Long employeeId) {
        if (isAdmin) {
            if (employeeId == null) {
                throw new IllegalArgumentException("employeeId is required for admin reports");
            }
            return employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        }
        return employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmployeeProfileNotLinkedException(userEmail));
    }

    private boolean isSundayOrHoliday(final LocalDate workDate) {
        return workDate.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidayRepository.existsByHolidayDate(workDate);
    }
}

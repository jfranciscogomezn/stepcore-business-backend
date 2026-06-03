package com.stepcore.business.report.controller;

import com.stepcore.business.report.dto.TimeReportResponse;
import com.stepcore.business.report.service.TimeReportService;
import com.stepcore.business.security.AppPermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/reports/time")
@RequiredArgsConstructor
public class TimeReportController {

    private final TimeReportService timeReportService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + AppPermissions.REPORTS + "', '" + AppPermissions.MY_TIME + "')")
    public TimeReportResponse getCappedReport(
            final Authentication authentication,
            @RequestParam(required = false) final Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") final YearMonth month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate week,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate) {
        return buildReport(authentication, employeeId, date, month, week, startDate, endDate, true);
    }

    @GetMapping("/uncapped")
    @PreAuthorize("hasAuthority('" + AppPermissions.REPORTS + "')")
    public TimeReportResponse getUncappedReport(
            final Authentication authentication,
            @RequestParam final Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") final YearMonth month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate week,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate) {
        return buildReport(authentication, employeeId, date, month, week, startDate, endDate, false);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('" + AppPermissions.REPORTS + "', '" + AppPermissions.MY_TIME + "')")
    public ResponseEntity<byte[]> exportReport(
            final Authentication authentication,
            @RequestParam(required = false) final Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") final YearMonth month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate week,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate,
            @RequestParam(defaultValue = "true") final boolean cap) {
        final TimeReportResponse report = buildReport(
                authentication, employeeId, date, month, week, startDate, endDate, cap);
        final byte[] content = timeReportService.exportExcel(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=time-report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    private TimeReportResponse buildReport(
            final Authentication authentication,
            final Long employeeId,
            final LocalDate date,
            final YearMonth month,
            final LocalDate week,
            final LocalDate startDate,
            final LocalDate endDate,
            final boolean capped) {
        final boolean isAdmin = hasAuthority(authentication, AppPermissions.REPORTS);
        if (!isAdmin && employeeId != null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Employees cannot request reports for other employees");
        }
        return timeReportService.buildReport(
                authentication.getName(),
                isAdmin,
                employeeId,
                date,
                month,
                week,
                startDate,
                endDate,
                capped);
    }

    private boolean hasAuthority(final Authentication authentication, final String authority) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}

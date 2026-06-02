package com.stepcore.business.time.controller;

import com.stepcore.business.security.AppPermissions;
import com.stepcore.business.time.controller.dto.TimeRecordResponse;
import com.stepcore.business.time.service.TimeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/time-records")
@RequiredArgsConstructor
public class TimeRecordController {

    private final TimeRecordService timeRecordService;

    @PostMapping("/clock-in")
    @PreAuthorize("hasAuthority('" + AppPermissions.MY_TIME + "')")
    public ResponseEntity<TimeRecordResponse> clockIn(final Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeRecordService.clockIn(authentication.getName()));
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasAuthority('" + AppPermissions.MY_TIME + "')")
    public TimeRecordResponse clockOut(final Authentication authentication) {
        return timeRecordService.clockOut(authentication.getName());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('" + AppPermissions.MY_TIME + "')")
    public List<TimeRecordResponse> getMyRecords(
            final Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return timeRecordService.getMyRecords(authentication.getName(), from, to);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public List<TimeRecordResponse> getEmployeeRecords(
            @RequestParam final Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return timeRecordService.getEmployeeRecords(employeeId, from, to);
    }
}

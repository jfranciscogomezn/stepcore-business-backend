package com.stepcore.business.time.controller;

import com.stepcore.business.security.AppPermissions;
import com.stepcore.business.time.controller.dto.CreateTimeRecordRequest;
import com.stepcore.business.time.controller.dto.CorrectTimeRecordRequest;
import com.stepcore.business.time.controller.dto.ResolveIncompleteRequest;
import com.stepcore.business.time.controller.dto.TimeRecordResponse;
import com.stepcore.business.time.service.TimeRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/incomplete")
    @PreAuthorize("hasAnyAuthority('" + AppPermissions.MY_TIME + "', '" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public List<TimeRecordResponse> getIncompleteRecords(
            final Authentication authentication,
            @RequestParam(required = false) final Long employeeId) {
        final boolean isAdmin = hasAuthority(authentication, AppPermissions.TIME_RECORDS_ADMIN);
        if (!isAdmin && employeeId != null) {
            throw new org.springframework.security.access.AccessDeniedException("Employees cannot filter incomplete records by employee");
        }
        return timeRecordService.getIncompleteRecords(authentication.getName(), isAdmin, employeeId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public List<TimeRecordResponse> getEmployeeRecords(
            @RequestParam final Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return timeRecordService.getEmployeeRecords(employeeId, from, to);
    }

    @PatchMapping("/{id}/reopen")
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public TimeRecordResponse reopen(
            final Authentication authentication,
            @PathVariable final Long id) {
        return timeRecordService.reopen(authentication.getName(), id);
    }

    @PatchMapping("/{id}/resolve-incomplete")
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public TimeRecordResponse resolveIncomplete(
            final Authentication authentication,
            @PathVariable final Long id,
            @Valid @RequestBody final ResolveIncompleteRequest request) {
        return timeRecordService.resolveIncomplete(authentication.getName(), id, request);
    }

    @PutMapping("/{id}/correct")
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public TimeRecordResponse correctRecord(
            final Authentication authentication,
            @PathVariable final Long id,
            @Valid @RequestBody final CorrectTimeRecordRequest request) {
        return timeRecordService.correctRecord(authentication.getName(), id, request);
    }

    @PostMapping("/correct")
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public ResponseEntity<TimeRecordResponse> createCorrectedRecord(
            final Authentication authentication,
            @Valid @RequestBody final CreateTimeRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeRecordService.createCorrectedRecord(authentication.getName(), request));
    }

    private boolean hasAuthority(final Authentication authentication, final String authority) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}

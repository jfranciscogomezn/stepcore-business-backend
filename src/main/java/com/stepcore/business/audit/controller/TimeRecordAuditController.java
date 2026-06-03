package com.stepcore.business.audit.controller;

import com.stepcore.business.audit.controller.dto.TimeRecordAuditEntryResponse;
import com.stepcore.business.audit.service.TimeRecordAuditService;
import com.stepcore.business.security.AppPermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit/time-records")
@RequiredArgsConstructor
public class TimeRecordAuditController {

    private final TimeRecordAuditService timeRecordAuditService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + AppPermissions.TIME_RECORDS_ADMIN + "')")
    public List<TimeRecordAuditEntryResponse> listRecent(
            @RequestParam(defaultValue = "50") final int limit) {
        return timeRecordAuditService.listRecent(limit);
    }
}

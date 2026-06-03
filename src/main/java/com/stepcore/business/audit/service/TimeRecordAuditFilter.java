package com.stepcore.business.audit.service;

import java.time.LocalDate;

public record TimeRecordAuditFilter(
        LocalDate fromDate,
        LocalDate toDate,
        Long employeeId,
        Long userId,
        int limit
) {
}

package com.stepcore.business.audit.model;

import java.time.Instant;
import java.time.LocalDate;

public record TimeRecordAuditSnapshot(
        Long employeeId,
        LocalDate workDate,
        Instant clockIn,
        Instant clockOut
) {
}

package com.stepcore.business.support;

import com.stepcore.business.time.controller.dto.CreateTimeRecordRequest;

import java.time.Instant;
import java.time.LocalDate;

public final class TimeReportTestSupport {

    private TimeReportTestSupport() {
    }

    public static CreateTimeRecordRequest closedRecord(final Long employeeId, final LocalDate workDate) {
        return new CreateTimeRecordRequest(
                employeeId,
                workDate,
                Instant.parse(workDate + "T13:00:00Z"),
                Instant.parse(workDate + "T22:00:00Z"),
                "Seeded closed record for report integration test");
    }
}

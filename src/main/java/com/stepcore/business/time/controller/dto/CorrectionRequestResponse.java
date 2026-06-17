package com.stepcore.business.time.controller.dto;

import java.time.Instant;
import java.time.LocalDate;

public record CorrectionRequestResponse(
        Long id,
        Long timeRecordId,
        Long employeeId,
        String employeeName,
        LocalDate recordDate,
        String note,
        String status,
        String resolutionNote,
        Instant createdAt,
        Instant resolvedAt
) {
}

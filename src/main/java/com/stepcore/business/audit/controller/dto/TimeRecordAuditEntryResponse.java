package com.stepcore.business.audit.controller.dto;

import java.time.LocalDateTime;

public record TimeRecordAuditEntryResponse(
        Long id,
        String action,
        String entityId,
        String actorEmail,
        String oldValue,
        String newValue,
        String details,
        LocalDateTime createdAt
) {
}

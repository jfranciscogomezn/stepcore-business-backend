package com.stepcore.business.audit.controller.dto;

import java.time.LocalDateTime;

public record TimeRecordAuditEntryResponse(
        Long id,
        String action,
        String entityId,
        Long actorUserId,
        String actorEmail,
        String oldValue,
        String newValue,
        String correctionReason,
        String details,
        LocalDateTime createdAt
) {
}

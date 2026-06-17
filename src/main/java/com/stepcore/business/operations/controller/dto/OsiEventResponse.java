package com.stepcore.business.operations.controller.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OsiEventResponse(
        Long id,
        Long osiId,
        Long vehicleId,
        Long eventTypeId,
        String eventTypeName,
        Long authorUserId,
        String text,
        OffsetDateTime capturedAtLocal,
        OffsetDateTime receivedAt,
        BigDecimal geoLat,
        BigDecimal geoLng,
        String effectiveVisibility,
        Long parentEventId,
        String correctionReason,
        UUID idempotencyKey,
        String externalPartyName,
        String externalPartyDocument,
        List<AttachmentResponse> attachments
) {}

package com.stepcore.business.operations.controller.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PortalEventResponse(
        Long id,
        String eventTypeName,
        String text,
        OffsetDateTime capturedAtLocal,
        OffsetDateTime receivedAt,
        BigDecimal geoLat,
        BigDecimal geoLng,
        List<PortalAttachmentResponse> attachments
) {}

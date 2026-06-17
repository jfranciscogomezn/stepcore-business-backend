package com.stepcore.business.operations.controller.dto;

import java.time.OffsetDateTime;

public record EventTypeResponse(
        Long id,
        String name,
        String description,
        String defaultVisibility,
        int minAttachments,
        int maxAttachments,
        boolean hasMeasurementForm,
        boolean active,
        OffsetDateTime createdAt
) {}

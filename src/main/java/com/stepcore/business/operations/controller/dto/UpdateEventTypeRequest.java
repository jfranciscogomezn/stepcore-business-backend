package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.Size;

public record UpdateEventTypeRequest(
        @Size(max = 100) String name,
        String description,
        String defaultVisibility,
        Integer minAttachments,
        Integer maxAttachments,
        Boolean hasMeasurementForm,
        Boolean active
) {}

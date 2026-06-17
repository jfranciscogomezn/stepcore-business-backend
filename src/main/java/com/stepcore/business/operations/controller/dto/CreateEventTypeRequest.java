package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEventTypeRequest(
        @NotBlank @Size(max = 100) String name,
        String description,
        String defaultVisibility,
        @Min(0) int minAttachments,
        @Min(0) int maxAttachments,
        boolean hasMeasurementForm
) {}

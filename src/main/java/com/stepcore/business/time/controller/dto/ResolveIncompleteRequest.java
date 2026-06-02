package com.stepcore.business.time.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ResolveIncompleteRequest(
        @NotNull(message = "Clock-out time is required")
        Instant clockOut,
        @NotBlank(message = "Correction note is required")
        String note
) {}

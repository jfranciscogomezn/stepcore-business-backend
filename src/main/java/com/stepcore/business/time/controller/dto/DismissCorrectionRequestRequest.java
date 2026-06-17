package com.stepcore.business.time.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DismissCorrectionRequestRequest(
        @NotBlank(message = "Dismissal reason is required")
        @Size(max = 1000, message = "Dismissal reason must be at most 1000 characters")
        String dismissalReason
) {
}

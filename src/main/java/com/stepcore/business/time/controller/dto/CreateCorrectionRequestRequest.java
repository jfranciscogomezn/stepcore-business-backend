package com.stepcore.business.time.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCorrectionRequestRequest(
        @NotBlank(message = "Note is required")
        @Size(max = 1000, message = "Note must be at most 1000 characters")
        String note
) {
}

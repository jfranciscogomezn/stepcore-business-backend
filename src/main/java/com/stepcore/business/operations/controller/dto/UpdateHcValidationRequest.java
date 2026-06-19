package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateHcValidationRequest(
        @NotNull String status,
        @Size(max = 1000) String notes
) {}

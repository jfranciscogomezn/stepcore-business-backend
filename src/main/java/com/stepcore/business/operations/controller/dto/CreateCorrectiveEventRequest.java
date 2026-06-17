package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCorrectiveEventRequest(
        @NotNull Long eventTypeId,
        @NotBlank @Size(max = 2000) String text,
        @NotBlank @Size(max = 500) String correctionReason
) {}

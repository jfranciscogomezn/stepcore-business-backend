package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateOsiRequest(
        @NotNull Long clientId,
        @NotBlank @Size(max = 300) String origin,
        @NotBlank @Size(max = 300) String destination,
        OffsetDateTime loadWindowStart,
        OffsetDateTime loadWindowEnd,
        OffsetDateTime deliveryWindowStart,
        OffsetDateTime deliveryWindowEnd,
        @Size(max = 150) String commercialReference,
        String internalNotes
) {}

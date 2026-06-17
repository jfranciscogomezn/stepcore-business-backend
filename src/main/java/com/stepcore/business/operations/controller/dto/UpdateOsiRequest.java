package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record UpdateOsiRequest(
        Long clientId,
        @Size(max = 300) String origin,
        @Size(max = 300) String destination,
        OffsetDateTime loadWindowStart,
        OffsetDateTime loadWindowEnd,
        OffsetDateTime deliveryWindowStart,
        OffsetDateTime deliveryWindowEnd,
        @Size(max = 150) String commercialReference,
        String internalNotes,
        String status
) {}

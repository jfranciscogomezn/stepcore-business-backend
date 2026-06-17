package com.stepcore.business.operations.controller.dto;

import java.time.OffsetDateTime;

public record OsiSummaryResponse(
        Long id,
        String osiNumber,
        Long clientId,
        String clientName,
        String origin,
        String destination,
        String status,
        OffsetDateTime createdAt,
        int vehicleCount
) {}

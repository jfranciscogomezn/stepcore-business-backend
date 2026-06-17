package com.stepcore.business.operations.controller.dto;

import java.time.OffsetDateTime;

public record VehicleResponse(
        Long id,
        String plate,
        String type,
        String brand,
        String model,
        Integer year,
        String status,
        String internalNotes,
        OffsetDateTime createdAt
) {}

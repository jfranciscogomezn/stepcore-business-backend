package com.stepcore.business.operations.controller.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record OsiResponse(
        Long id,
        String osiNumber,
        Long clientId,
        String clientName,
        String origin,
        String destination,
        OffsetDateTime loadWindowStart,
        OffsetDateTime loadWindowEnd,
        OffsetDateTime deliveryWindowStart,
        OffsetDateTime deliveryWindowEnd,
        String commercialReference,
        String internalNotes,
        String status,
        Long coordinatorUserId,
        OffsetDateTime createdAt,
        OffsetDateTime closedAt,
        List<OsiVehicleAssignmentResponse> assignments
) {}

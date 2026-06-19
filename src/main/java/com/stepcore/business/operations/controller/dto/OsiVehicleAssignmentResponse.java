package com.stepcore.business.operations.controller.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record OsiVehicleAssignmentResponse(
        Long id,
        Long vehicleId,
        String vehiclePlate,
        String state,
        List<Long> assignedUserIds,
        String gpsProvider,
        String gpsReferenceUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignVehicleRequest(
        @NotNull Long vehicleId,
        List<Long> assignedUserIds
) {}

package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.Size;

public record UpdateVehicleRequest(
        String type,
        @Size(max = 80) String brand,
        @Size(max = 80) String model,
        Integer year,
        String status,
        String internalNotes
) {}

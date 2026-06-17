package com.stepcore.business.operations.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateVehicleRequest(
        @NotBlank @Size(max = 10) String plate,
        String type,
        @Size(max = 80) String brand,
        @Size(max = 80) String model,
        Integer year,
        String internalNotes
) {}

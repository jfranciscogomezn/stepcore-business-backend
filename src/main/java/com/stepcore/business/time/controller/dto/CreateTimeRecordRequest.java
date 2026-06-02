package com.stepcore.business.time.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;

public record CreateTimeRecordRequest(
        @NotNull(message = "Employee id is required")
        Long employeeId,
        @NotNull(message = "Work date is required")
        LocalDate workDate,
        @NotNull(message = "Clock-in time is required")
        Instant clockIn,
        @NotNull(message = "Clock-out time is required")
        Instant clockOut,
        @NotBlank(message = "Correction reason is required")
        String correctionReason
) {}

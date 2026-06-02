package com.stepcore.business.time.controller.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record CorrectTimeRecordRequest(
        Instant clockIn,
        Instant clockOut,
        @NotBlank(message = "Correction reason is required")
        String correctionReason
) {}

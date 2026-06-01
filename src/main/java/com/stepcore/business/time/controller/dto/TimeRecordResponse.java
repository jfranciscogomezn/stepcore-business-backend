package com.stepcore.business.time.controller.dto;

import com.stepcore.business.time.domain.model.TimeRecordStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TimeRecordResponse(
        Long id,
        Long employeeId,
        LocalDate workDate,
        Instant clockIn,
        Instant clockOut,
        TimeRecordStatus status,
        boolean corrected
) {}

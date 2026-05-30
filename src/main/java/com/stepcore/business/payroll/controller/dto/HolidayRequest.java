package com.stepcore.business.payroll.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record HolidayRequest(
        @NotNull LocalDate date,
        @Size(max = 150) String description
) {
}

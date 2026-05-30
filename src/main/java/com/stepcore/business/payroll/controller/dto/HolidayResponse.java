package com.stepcore.business.payroll.controller.dto;

import java.time.LocalDate;

public record HolidayResponse(
        Long id,
        LocalDate date,
        String description
) {
}

package com.stepcore.business.employee.controller.dto;

import com.stepcore.business.employee.domain.model.IdType;

import java.math.BigDecimal;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        IdType idType,
        String idNumber,
        String email,
        String phone,
        BigDecimal monthlySalary,
        Long userId
) {
}

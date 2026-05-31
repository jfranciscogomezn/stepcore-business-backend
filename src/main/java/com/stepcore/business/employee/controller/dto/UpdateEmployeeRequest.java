package com.stepcore.business.employee.controller.dto;

import com.stepcore.business.employee.domain.model.IdType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateEmployeeRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull IdType idType,
        @NotBlank @Size(max = 50) String idNumber,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 20) String phone,
        @NotNull @DecimalMin("0.01") BigDecimal monthlySalary,
        Long userId
) {
}

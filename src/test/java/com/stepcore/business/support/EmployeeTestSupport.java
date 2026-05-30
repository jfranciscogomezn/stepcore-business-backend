package com.stepcore.business.support;

import com.stepcore.business.employee.controller.dto.CreateEmployeeRequest;
import com.stepcore.business.employee.domain.model.IdType;

import java.math.BigDecimal;

public final class EmployeeTestSupport {

    private EmployeeTestSupport() {
    }

    public static CreateEmployeeRequest validCreateRequest() {
        return new CreateEmployeeRequest(
                "Ana",
                "García",
                IdType.CC,
                "1234567890",
                "ana.garcia@example.com",
                "3001234567",
                new BigDecimal("3500000.00"),
                null
        );
    }
}

package com.stepcore.business.support;

import com.stepcore.business.payroll.controller.dto.PayrollConfigRequest;

import java.math.BigDecimal;
import java.time.LocalTime;

public final class PayrollConfigTestSupport {

    private PayrollConfigTestSupport() {
    }

    public static PayrollConfigRequest validRequest() {
        return new PayrollConfigRequest(
                new BigDecimal("1423500.00"),
                new BigDecimal("200000.00"),
                new BigDecimal("240.00"),
                new BigDecimal("8.00"),
                new BigDecimal("2.00"),
                LocalTime.of(6, 0),
                LocalTime.of(21, 0),
                LocalTime.of(6, 0),
                LocalTime.of(21, 0),
                LocalTime.of(21, 0),
                LocalTime.of(6, 0),
                LocalTime.of(21, 0),
                LocalTime.of(6, 0),
                LocalTime.of(6, 0),
                LocalTime.of(21, 0),
                new BigDecimal("1.25"),
                new BigDecimal("1.75"),
                new BigDecimal("1.35"),
                new BigDecimal("2.00"),
                new BigDecimal("2.50"),
                new BigDecimal("1.75"),
                60
        );
    }
}

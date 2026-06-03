package com.stepcore.business.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record TimeReportResponse(
        Long employeeId,
        String employeeName,
        boolean capped,
        List<TimeReportRecordResponse> records,
        BigDecimal totalUncappedEarnings,
        BigDecimal totalCappedEarnings
) {}

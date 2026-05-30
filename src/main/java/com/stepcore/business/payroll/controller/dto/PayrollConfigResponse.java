package com.stepcore.business.payroll.controller.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record PayrollConfigResponse(
        int year,
        BigDecimal minimumWage,
        BigDecimal transportSubsidy,
        BigDecimal monthlyWorkHours,
        BigDecimal normalDailyHours,
        BigDecimal maxDailyExtraHours,
        LocalTime daytimeStart,
        LocalTime daytimeEnd,
        LocalTime daytimeOtStart,
        LocalTime daytimeOtEnd,
        LocalTime nightSurchargeStart,
        LocalTime nightSurchargeEnd,
        LocalTime nocturnalOtStart,
        LocalTime nocturnalOtEnd,
        LocalTime sundayOtStart,
        LocalTime sundayOtEnd,
        BigDecimal daytimeOtFactor,
        BigDecimal nocturnalOtFactor,
        BigDecimal nightSurchargeFactor,
        BigDecimal sundayHolidayDaytimeOtFactor,
        BigDecimal sundayHolidayNocturnalOtFactor,
        BigDecimal sundayHolidayNormalFactor,
        int nonBillableRestMinutes
) {
}

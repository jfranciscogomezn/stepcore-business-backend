package com.stepcore.business.payroll.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;

public record PayrollConfigRequest(
        @NotNull @DecimalMin("0.0") BigDecimal minimumWage,
        @NotNull @DecimalMin("0.0") BigDecimal transportSubsidy,
        @NotNull @DecimalMin("0.01") BigDecimal monthlyWorkHours,
        @NotNull @DecimalMin("0.01") BigDecimal normalDailyHours,
        @NotNull @DecimalMin("0.0") BigDecimal maxDailyExtraHours,
        @NotNull LocalTime daytimeStart,
        @NotNull LocalTime daytimeEnd,
        @NotNull LocalTime daytimeOtStart,
        @NotNull LocalTime daytimeOtEnd,
        @NotNull LocalTime nightSurchargeStart,
        @NotNull LocalTime nightSurchargeEnd,
        @NotNull LocalTime nocturnalOtStart,
        @NotNull LocalTime nocturnalOtEnd,
        @NotNull LocalTime sundayOtStart,
        @NotNull LocalTime sundayOtEnd,
        @NotNull @DecimalMin("0.0") BigDecimal daytimeOtFactor,
        @NotNull @DecimalMin("0.0") BigDecimal nocturnalOtFactor,
        @NotNull @DecimalMin("0.0") BigDecimal nightSurchargeFactor,
        @NotNull @DecimalMin("0.0") BigDecimal sundayHolidayDaytimeOtFactor,
        @NotNull @DecimalMin("0.0") BigDecimal sundayHolidayNocturnalOtFactor,
        @NotNull @DecimalMin("0.0") BigDecimal sundayHolidayNormalFactor,
        @NotNull @Min(0) Integer nonBillableRestMinutes
) {
}

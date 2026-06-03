package com.stepcore.business.earnings;

import com.stepcore.business.payroll.domain.model.PayrollConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class EarningsCalculationServiceTest {

    private EarningsCalculationService service;
    private PayrollConfig config;

    @BeforeEach
    void setUp() {
        service = new EarningsCalculationService();
        config = PayrollConfig.builder()
                .withYear(2026)
                .withMinimumWage(new BigDecimal("1423500"))
                .withTransportSubsidy(new BigDecimal("200000"))
                .withMonthlyWorkHours(new BigDecimal("240"))
                .withNormalDailyHours(new BigDecimal("8"))
                .withMaxDailyExtraHours(new BigDecimal("2"))
                .withDaytimeStart(LocalTime.of(6, 0))
                .withDaytimeEnd(LocalTime.of(18, 0))
                .withDaytimeOtStart(LocalTime.of(18, 0))
                .withDaytimeOtEnd(LocalTime.of(21, 0))
                .withNightSurchargeStart(LocalTime.of(21, 0))
                .withNightSurchargeEnd(LocalTime.of(6, 0))
                .withNocturnalOtStart(LocalTime.of(21, 0))
                .withNocturnalOtEnd(LocalTime.of(6, 0))
                .withSundayOtStart(LocalTime.of(6, 0))
                .withSundayOtEnd(LocalTime.of(21, 0))
                .withDaytimeOtFactor(new BigDecimal("1.25"))
                .withNocturnalOtFactor(new BigDecimal("1.75"))
                .withNightSurchargeFactor(new BigDecimal("1.35"))
                .withSundayHolidayDaytimeOtFactor(new BigDecimal("2.00"))
                .withSundayHolidayNocturnalOtFactor(new BigDecimal("2.50"))
                .withSundayHolidayNormalFactor(new BigDecimal("1.75"))
                .withNonBillableRestMinutes(60)
                .build();
    }

    @Test
    void shouldComputeHourlyRateFromSalaryAndMonthlyHours() {
        final BigDecimal hourlyRate = service.hourlyRate(new BigDecimal("3000000"), new BigDecimal("240"));
        assertThat(hourlyRate).isEqualByComparingTo("12500.000000");
    }

    @Test
    void shouldDeductRestMinutesBeforeClassification() {
        final Instant clockIn = Instant.parse("2026-05-30T13:00:00Z"); // 08:00 Bogota
        final Instant clockOut = Instant.parse("2026-05-30T22:00:00Z"); // 17:00 Bogota

        final var result = service.calculateDailyEarnings(
                clockIn, clockOut, LocalDate.of(2026, 5, 30),
                new BigDecimal("3000000"), config, false);

        assertThat(result.classifiedMinutes().totalBillableMinutes()).isEqualTo(8 * 60);
        assertThat(result.classifiedMinutes().normal()).isEqualTo(8 * 60);
    }

    @Test
    void shouldApplyDaytimeOvertimeFactor() {
        final Instant clockIn = Instant.parse("2026-05-30T13:00:00Z"); // 08:00
        final Instant clockOut = Instant.parse("2026-05-31T01:00:00Z"); // 20:00

        final var result = service.calculateDailyEarnings(
                clockIn, clockOut, LocalDate.of(2026, 5, 30),
                new BigDecimal("3000000"), config, false);

        assertThat(result.classifiedMinutes().daytimeOt()).isGreaterThan(0);
        assertThat(result.uncappedEarnings()).isGreaterThan(result.cappedEarnings());
    }

    @Test
    void shouldCapNormalAndDaytimeOvertimeForCappedView() {
        final Instant clockIn = Instant.parse("2026-05-30T11:00:00Z"); // 06:00
        final Instant clockOut = Instant.parse("2026-05-31T00:00:00Z"); // 19:00

        final var result = service.calculateDailyEarnings(
                clockIn, clockOut, LocalDate.of(2026, 5, 30),
                new BigDecimal("3000000"), config, false);

        assertThat(result.cappedMinutes().normal()).isLessThanOrEqualTo(8 * 60);
        assertThat(result.cappedMinutes().daytimeOt()).isLessThanOrEqualTo(2 * 60);
    }

    @Test
    void shouldApplySundayHolidayFactor() {
        final Instant clockIn = Instant.parse("2026-05-31T13:00:00Z");
        final Instant clockOut = Instant.parse("2026-05-31T22:00:00Z");

        final var weekday = service.calculateDailyEarnings(
                clockIn, clockOut, LocalDate.of(2026, 5, 30),
                new BigDecimal("3000000"), config, false);
        final var sunday = service.calculateDailyEarnings(
                clockIn, clockOut, LocalDate.of(2026, 5, 31),
                new BigDecimal("3000000"), config, true);

        assertThat(sunday.uncappedEarnings()).isGreaterThan(weekday.uncappedEarnings());
    }

    @Test
    void shouldSetAlertHighlightWhenBillableHoursExceedNormalPlusExtra() {
        final Instant clockIn = Instant.parse("2026-05-30T11:00:00Z");
        final Instant clockOut = Instant.parse("2026-05-31T02:00:00Z");

        final var result = service.calculateDailyEarnings(
                clockIn, clockOut, LocalDate.of(2026, 5, 30),
                new BigDecimal("3000000"), config, false);

        assertThat(result.highlightLevel().name()).isIn("WARNING", "ALERT");
    }
}

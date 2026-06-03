package com.stepcore.business.report.support;

import com.stepcore.business.exception.InvalidReportPeriodException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportPeriodResolverTest {

    private final ReportPeriodResolver resolver = new ReportPeriodResolver();

    @Test
    void shouldResolveSingleDay() {
        final var period = resolver.resolve(LocalDate.of(2026, 6, 1), null, null, null, null);
        assertThat(period.start()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(period.end()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    void shouldResolveMonth() {
        final var period = resolver.resolve(null, YearMonth.of(2026, 6), null, null, null);
        assertThat(period.start()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(period.end()).isEqualTo(LocalDate.of(2026, 6, 30));
    }

    @Test
    void shouldRejectMultipleFilterModes() {
        assertThatThrownBy(() -> resolver.resolve(
                LocalDate.of(2026, 6, 1),
                YearMonth.of(2026, 6),
                null,
                null,
                null))
                .isInstanceOf(InvalidReportPeriodException.class);
    }
}

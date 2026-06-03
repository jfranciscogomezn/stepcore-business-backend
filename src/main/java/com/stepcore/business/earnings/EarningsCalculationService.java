package com.stepcore.business.earnings;

import com.stepcore.business.earnings.model.ClassifiedMinutes;
import com.stepcore.business.earnings.model.EarningsResult;
import com.stepcore.business.earnings.model.HighlightLevel;
import com.stepcore.business.earnings.model.TimeBand;
import com.stepcore.business.payroll.domain.model.PayrollConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
public class EarningsCalculationService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Bogota");
    private static final int MONEY_SCALE = 2;

    public EarningsResult calculateDailyEarnings(
            final Instant clockIn,
            final Instant clockOut,
            final LocalDate workDate,
            final BigDecimal monthlySalary,
            final PayrollConfig config,
            final boolean sundayOrHoliday) {

        final int workedMinutes = (int) ((clockOut.toEpochMilli() - clockIn.toEpochMilli()) / 60_000L);
        final int billableMinutes = Math.max(0, workedMinutes - config.getNonBillableRestMinutes());
        final ClassifiedMinutes classified = classifyMinutes(
                clockIn, billableMinutes, config, sundayOrHoliday);

        final int maxNormalMinutes = toMinutes(config.getNormalDailyHours());
        final int maxDaytimeOtMinutes = toMinutes(config.getMaxDailyExtraHours());
        final ClassifiedMinutes capped = classified.capped(maxNormalMinutes, maxDaytimeOtMinutes);

        final BigDecimal hourlyRate = hourlyRate(monthlySalary, config.getMonthlyWorkHours());
        final BigDecimal uncapped = computeAmount(classified, hourlyRate, config, sundayOrHoliday);
        final BigDecimal cappedAmount = computeAmount(capped, hourlyRate, config, sundayOrHoliday);

        return new EarningsResult(
                hourlyRate,
                classified,
                capped,
                uncapped,
                cappedAmount,
                highlightLevel(classified, config));
    }

    public BigDecimal hourlyRate(final BigDecimal monthlySalary, final BigDecimal monthlyWorkHours) {
        return monthlySalary.divide(monthlyWorkHours, 6, RoundingMode.HALF_UP);
    }

    private ClassifiedMinutes classifyMinutes(
            final Instant clockIn,
            final int billableMinutes,
            final PayrollConfig config,
            final boolean sundayOrHoliday) {
        if (billableMinutes <= 0) {
            return ClassifiedMinutes.ZERO;
        }

        final Map<TimeBand, Integer> counts = new EnumMap<>(TimeBand.class);
        for (final TimeBand band : TimeBand.values()) {
            counts.put(band, 0);
        }

        ZonedDateTime cursor = clockIn.atZone(DEFAULT_ZONE);
        for (int minute = 0; minute < billableMinutes; minute++) {
            final LocalTime localTime = cursor.toLocalTime();
            final TimeBand band = resolveBand(localTime, config, sundayOrHoliday);
            counts.put(band, counts.get(band) + 1);
            cursor = cursor.plusMinutes(1);
        }

        return new ClassifiedMinutes(
                counts.get(TimeBand.NORMAL),
                counts.get(TimeBand.DAYTIME_OT),
                counts.get(TimeBand.NIGHT_SURCHARGE),
                counts.get(TimeBand.NOCTURNAL_OT));
    }

    private TimeBand resolveBand(
            final LocalTime time,
            final PayrollConfig config,
            final boolean sundayOrHoliday) {
        if (sundayOrHoliday) {
            if (withinRange(time, config.getSundayOtStart(), config.getSundayOtEnd())) {
                return TimeBand.DAYTIME_OT;
            }
            return TimeBand.NORMAL;
        }
        if (withinRange(time, config.getNocturnalOtStart(), config.getNocturnalOtEnd())) {
            return TimeBand.NOCTURNAL_OT;
        }
        if (withinRange(time, config.getNightSurchargeStart(), config.getNightSurchargeEnd())) {
            return TimeBand.NIGHT_SURCHARGE;
        }
        if (withinRange(time, config.getDaytimeOtStart(), config.getDaytimeOtEnd())) {
            return TimeBand.DAYTIME_OT;
        }
        if (withinRange(time, config.getDaytimeStart(), config.getDaytimeEnd())) {
            return TimeBand.NORMAL;
        }
        return TimeBand.NORMAL;
    }

    private boolean withinRange(final LocalTime time, final LocalTime start, final LocalTime end) {
        if (start.equals(end)) {
            return false;
        }
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        }
        return !time.isBefore(start) || time.isBefore(end);
    }

    private BigDecimal computeAmount(
            final ClassifiedMinutes minutes,
            final BigDecimal hourlyRate,
            final PayrollConfig config,
            final boolean sundayOrHoliday) {
        return amountForBand(minutes.normal(), hourlyRate, factor(config, TimeBand.NORMAL, sundayOrHoliday))
                .add(amountForBand(minutes.daytimeOt(), hourlyRate, factor(config, TimeBand.DAYTIME_OT, sundayOrHoliday)))
                .add(amountForBand(minutes.nightSurcharge(), hourlyRate, factor(config, TimeBand.NIGHT_SURCHARGE, sundayOrHoliday)))
                .add(amountForBand(minutes.nocturnalOt(), hourlyRate, factor(config, TimeBand.NOCTURNAL_OT, sundayOrHoliday)))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal factor(final PayrollConfig config, final TimeBand band, final boolean sundayOrHoliday) {
        if (sundayOrHoliday) {
            return switch (band) {
                case NORMAL -> config.getSundayHolidayNormalFactor();
                case DAYTIME_OT -> config.getSundayHolidayDaytimeOtFactor();
                case NIGHT_SURCHARGE -> config.getSundayHolidayNormalFactor();
                case NOCTURNAL_OT -> config.getSundayHolidayNocturnalOtFactor();
            };
        }
        return switch (band) {
            case NORMAL -> BigDecimal.ONE;
            case DAYTIME_OT -> config.getDaytimeOtFactor();
            case NIGHT_SURCHARGE -> config.getNightSurchargeFactor();
            case NOCTURNAL_OT -> config.getNocturnalOtFactor();
        };
    }

    private BigDecimal amountForBand(final int minutes, final BigDecimal hourlyRate, final BigDecimal factor) {
        if (minutes <= 0) {
            return BigDecimal.ZERO;
        }
        final BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);
        return hours.multiply(hourlyRate).multiply(factor);
    }

    private HighlightLevel highlightLevel(final ClassifiedMinutes classified, final PayrollConfig config) {
        final BigDecimal billableHours = BigDecimal.valueOf(classified.totalBillableMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        final BigDecimal normalLimit = config.getNormalDailyHours();
        final BigDecimal alertLimit = normalLimit.add(config.getMaxDailyExtraHours());

        if (billableHours.compareTo(alertLimit) > 0) {
            return HighlightLevel.ALERT;
        }
        if (billableHours.compareTo(normalLimit) > 0) {
            return HighlightLevel.WARNING;
        }
        return HighlightLevel.NONE;
    }

    private int toMinutes(final BigDecimal hours) {
        return hours.multiply(BigDecimal.valueOf(60)).intValue();
    }
}

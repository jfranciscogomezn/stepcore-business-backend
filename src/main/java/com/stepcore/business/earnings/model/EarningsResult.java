package com.stepcore.business.earnings.model;

import java.math.BigDecimal;

public record EarningsResult(
        BigDecimal hourlyRate,
        ClassifiedMinutes classifiedMinutes,
        ClassifiedMinutes cappedMinutes,
        BigDecimal uncappedEarnings,
        BigDecimal cappedEarnings,
        HighlightLevel highlightLevel
) {}

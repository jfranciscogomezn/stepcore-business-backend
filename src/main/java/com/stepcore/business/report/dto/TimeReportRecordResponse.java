package com.stepcore.business.report.dto;

import com.stepcore.business.earnings.model.ClassifiedMinutes;
import com.stepcore.business.earnings.model.HighlightLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TimeReportRecordResponse(
        Long timeRecordId,
        LocalDate workDate,
        Instant clockIn,
        Instant clockOut,
        String status,
        boolean corrected,
        String correctionReason,
        ClassifiedMinutes classifiedMinutes,
        ClassifiedMinutes cappedMinutes,
        BigDecimal uncappedEarnings,
        BigDecimal cappedEarnings,
        HighlightLevel highlightLevel
) {}

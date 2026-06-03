package com.stepcore.business.report.service;

import com.stepcore.business.earnings.model.ClassifiedMinutes;
import com.stepcore.business.report.dto.TimeReportRecordResponse;
import com.stepcore.business.report.dto.TimeReportResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

@Component
public class TimeReportExcelExporter {

    public byte[] export(final TimeReportResponse report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final Sheet sheet = workbook.createSheet("Time Report");
            int rowIndex = 0;

            final Row header = sheet.createRow(rowIndex++);
            header.createCell(0).setCellValue("Employee");
            header.createCell(1).setCellValue(report.employeeName());
            header.createCell(2).setCellValue("Capped view");
            header.createCell(3).setCellValue(report.capped());

            final Row columns = sheet.createRow(rowIndex++);
            columns.createCell(0).setCellValue("Date");
            columns.createCell(1).setCellValue("Clock in");
            columns.createCell(2).setCellValue("Clock out");
            columns.createCell(3).setCellValue("Normal hours");
            columns.createCell(4).setCellValue("Daytime OT hours");
            columns.createCell(5).setCellValue("Earnings");
            columns.createCell(6).setCellValue("Notes");

            for (final TimeReportRecordResponse record : report.records()) {
                final ClassifiedMinutes minutes = report.capped() ? record.cappedMinutes() : record.classifiedMinutes();
                final BigDecimal earnings = report.capped() ? record.cappedEarnings() : record.uncappedEarnings();

                final Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(record.workDate().toString());
                row.createCell(1).setCellValue(record.clockIn().toString());
                row.createCell(2).setCellValue(record.clockOut().toString());
                row.createCell(3).setCellValue(toHours(minutes.normal()));
                row.createCell(4).setCellValue(toHours(minutes.daytimeOt()));
                row.createCell(5).setCellValue(earnings.doubleValue());
                row.createCell(6).setCellValue(buildNotes(record));
            }

            final Row totals = sheet.createRow(rowIndex);
            totals.createCell(0).setCellValue("TOTAL");
            totals.createCell(5).setCellValue(
                    (report.capped() ? report.totalCappedEarnings() : report.totalUncappedEarnings()).doubleValue());

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate Excel report", ex);
        }
    }

    private String buildNotes(final TimeReportRecordResponse record) {
        final StringBuilder notes = new StringBuilder();
        if (record.corrected() && record.correctionReason() != null && !record.correctionReason().isBlank()) {
            notes.append("Admin correction: ").append(record.correctionReason());
        }
        if (record.highlightLevel() == com.stepcore.business.earnings.model.HighlightLevel.ALERT) {
            if (!notes.isEmpty()) {
                notes.append("; ");
            }
            notes.append("Extended hours");
        }
        return notes.toString();
    }

    private double toHours(final int minutes) {
        return minutes / 60.0;
    }
}

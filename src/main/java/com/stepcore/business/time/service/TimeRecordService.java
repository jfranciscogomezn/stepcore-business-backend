package com.stepcore.business.time.service;

import com.stepcore.business.time.controller.dto.CreateTimeRecordRequest;
import com.stepcore.business.time.controller.dto.CorrectTimeRecordRequest;
import com.stepcore.business.time.controller.dto.ResolveIncompleteRequest;
import com.stepcore.business.time.controller.dto.TimeRecordResponse;

import com.stepcore.business.time.domain.model.TimeRecord;

import java.time.LocalDate;
import java.util.List;

public interface TimeRecordService {

    TimeRecordResponse clockIn(String userEmail);

    TimeRecordResponse clockOut(String userEmail);

    List<TimeRecordResponse> getMyRecords(String userEmail, LocalDate from, LocalDate to);

    List<TimeRecordResponse> getEmployeeRecords(Long employeeId, LocalDate from, LocalDate to);

    TimeRecordResponse reopen(Long recordId);

    TimeRecordResponse resolveIncomplete(Long recordId, ResolveIncompleteRequest request);

    List<TimeRecordResponse> getIncompleteRecords(String userEmail, boolean isAdmin, Long employeeId);

    TimeRecordResponse correctRecord(Long recordId, CorrectTimeRecordRequest request);

    TimeRecordResponse createCorrectedRecord(CreateTimeRecordRequest request);

    List<TimeRecord> flagStaleOpenRecordsAsIncomplete();
}

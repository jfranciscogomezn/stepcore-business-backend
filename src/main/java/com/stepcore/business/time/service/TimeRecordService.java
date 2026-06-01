package com.stepcore.business.time.service;

import com.stepcore.business.time.controller.dto.TimeRecordResponse;

import java.time.LocalDate;
import java.util.List;

public interface TimeRecordService {

    TimeRecordResponse clockIn(String userEmail);

    TimeRecordResponse clockOut(String userEmail);

    List<TimeRecordResponse> getMyRecords(String userEmail, LocalDate from, LocalDate to);

    List<TimeRecordResponse> getEmployeeRecords(Long employeeId, LocalDate from, LocalDate to);
}

package com.stepcore.business.time.service;

import com.stepcore.business.employee.domain.model.Employee;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.exception.DuplicateTimeRecordException;
import com.stepcore.business.exception.EmployeeNotFoundException;
import com.stepcore.business.exception.EmployeeProfileNotLinkedException;
import com.stepcore.business.exception.InvalidTimeRecordOperationException;
import com.stepcore.business.exception.TimeRecordNotFoundException;
import com.stepcore.business.time.controller.dto.CreateTimeRecordRequest;
import com.stepcore.business.time.controller.dto.CorrectTimeRecordRequest;
import com.stepcore.business.time.controller.dto.ResolveIncompleteRequest;
import com.stepcore.business.time.controller.dto.TimeRecordResponse;
import com.stepcore.business.time.controller.mapper.TimeRecordMapper;
import com.stepcore.business.time.domain.model.TimeRecord;
import com.stepcore.business.time.domain.model.TimeRecordStatus;
import com.stepcore.business.time.repository.TimeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TimeRecordServiceImpl implements TimeRecordService {

    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final TimeRecordMapper timeRecordMapper;

    @Override
    public TimeRecordResponse clockIn(final String userEmail) {
        final Employee employee = resolveEmployeeByEmail(userEmail);
        final LocalDate today = LocalDate.now();

        if (timeRecordRepository.existsByEmployeeIdAndWorkDate(employee.getId(), today)) {
            throw new DuplicateTimeRecordException("Employee already clocked in for today");
        }

        final TimeRecord record = TimeRecord.builder()
                .withEmployeeId(employee.getId())
                .withWorkDate(today)
                .withClockIn(Instant.now())
                .withStatus(TimeRecordStatus.OPEN)
                .build();

        final TimeRecord saved = timeRecordRepository.save(record);
        log.info("[TimeRecordServiceImpl] - CLOCK_IN: employeeId={} date={}", employee.getId(), today);
        return timeRecordMapper.toResponse(saved);
    }

    @Override
    public TimeRecordResponse clockOut(final String userEmail) {
        final Employee employee = resolveEmployeeByEmail(userEmail);
        final LocalDate today = LocalDate.now();

        final TimeRecord record = timeRecordRepository.findByEmployeeIdAndWorkDate(employee.getId(), today)
                .orElseThrow(() -> new InvalidTimeRecordOperationException("No clock-in record found for today"));

        if (!record.isOpen()) {
            throw new DuplicateTimeRecordException("Time record is already closed for today");
        }

        record.registerClockOut(Instant.now());
        final TimeRecord saved = timeRecordRepository.save(record);
        log.info("[TimeRecordServiceImpl] - CLOCK_OUT: employeeId={} date={}", employee.getId(), today);
        return timeRecordMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeRecordResponse> getMyRecords(
            final String userEmail,
            final LocalDate from,
            final LocalDate to) {
        final Employee employee = resolveEmployeeByEmail(userEmail);
        return listRecords(employee.getId(), from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeRecordResponse> getEmployeeRecords(
            final Long employeeId,
            final LocalDate from,
            final LocalDate to) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
        return listRecords(employeeId, from, to);
    }

    @Override
    public TimeRecordResponse reopen(final Long recordId) {
        final TimeRecord record = findRecordOrThrow(recordId);
        if (!record.isClosed()) {
            throw new InvalidTimeRecordOperationException("Only closed records can be reopened");
        }
        record.reopen();
        final TimeRecord saved = timeRecordRepository.save(record);
        log.info("[TimeRecordServiceImpl] - REOPEN: recordId={}", recordId);
        return timeRecordMapper.toResponse(saved);
    }

    @Override
    public TimeRecordResponse resolveIncomplete(final Long recordId, final ResolveIncompleteRequest request) {
        final TimeRecord record = findRecordOrThrow(recordId);
        if (!record.isIncomplete()) {
            throw new InvalidTimeRecordOperationException("Only incomplete records can be resolved");
        }
        validateClockOutAfterClockIn(record.getClockIn(), request.clockOut());
        record.resolveIncomplete(request.clockOut(), request.note().trim());
        final TimeRecord saved = timeRecordRepository.save(record);
        log.info("[TimeRecordServiceImpl] - RESOLVE_INCOMPLETE: recordId={}", recordId);
        return timeRecordMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeRecordResponse> getIncompleteRecords(
            final String userEmail,
            final boolean isAdmin,
            final Long employeeId) {
        if (isAdmin) {
            if (employeeId != null) {
                if (!employeeRepository.existsById(employeeId)) {
                    throw new EmployeeNotFoundException(employeeId);
                }
                return timeRecordRepository
                        .findByEmployeeIdAndStatusOrderByWorkDateDesc(employeeId, TimeRecordStatus.INCOMPLETE)
                        .stream()
                        .map(timeRecordMapper::toResponse)
                        .toList();
            }
            return timeRecordRepository.findByStatusOrderByWorkDateDesc(TimeRecordStatus.INCOMPLETE)
                    .stream()
                    .map(timeRecordMapper::toResponse)
                    .toList();
        }

        final Employee employee = resolveEmployeeByEmail(userEmail);
        return timeRecordRepository
                .findByEmployeeIdAndStatusOrderByWorkDateDesc(employee.getId(), TimeRecordStatus.INCOMPLETE)
                .stream()
                .map(timeRecordMapper::toResponse)
                .toList();
    }

    @Override
    public TimeRecordResponse correctRecord(final Long recordId, final CorrectTimeRecordRequest request) {
        if (request.clockIn() == null && request.clockOut() == null) {
            throw new InvalidTimeRecordOperationException("At least one of clock-in or clock-out must be provided");
        }

        final TimeRecord record = findRecordOrThrow(recordId);
        final Instant nextClockIn = request.clockIn() != null ? request.clockIn() : record.getClockIn();
        final Instant nextClockOut = request.clockOut() != null ? request.clockOut() : record.getClockOut();

        if (nextClockOut == null) {
            throw new InvalidTimeRecordOperationException("Clock-out is required to close a corrected record");
        }

        validateClockOutAfterClockIn(nextClockIn, nextClockOut);
        record.applyCorrection(request.clockIn(), request.clockOut(), request.correctionReason().trim());
        final TimeRecord saved = timeRecordRepository.save(record);
        log.info("[TimeRecordServiceImpl] - CORRECT: recordId={}", recordId);
        return timeRecordMapper.toResponse(saved);
    }

    @Override
    public TimeRecordResponse createCorrectedRecord(final CreateTimeRecordRequest request) {
        if (!employeeRepository.existsById(request.employeeId())) {
            throw new EmployeeNotFoundException(request.employeeId());
        }
        if (timeRecordRepository.existsByEmployeeIdAndWorkDate(request.employeeId(), request.workDate())) {
            throw new DuplicateTimeRecordException("A time record already exists for this employee and date");
        }

        validateClockOutAfterClockIn(request.clockIn(), request.clockOut());

        final TimeRecord record = TimeRecord.builder()
                .withEmployeeId(request.employeeId())
                .withWorkDate(request.workDate())
                .withClockIn(request.clockIn())
                .withClockOut(request.clockOut())
                .withStatus(TimeRecordStatus.CLOSED)
                .withCorrected(true)
                .withCorrectionReason(request.correctionReason().trim())
                .build();

        final TimeRecord saved = timeRecordRepository.save(record);
        log.info("[TimeRecordServiceImpl] - CREATE_CORRECTED: employeeId={} date={}",
                request.employeeId(), request.workDate());
        return timeRecordMapper.toResponse(saved);
    }

    @Override
    public List<TimeRecord> flagStaleOpenRecordsAsIncomplete() {
        final LocalDate today = LocalDate.now();
        final List<TimeRecord> staleOpenRecords = timeRecordRepository
                .findByStatusAndWorkDateBeforeOrderByWorkDateAsc(TimeRecordStatus.OPEN, today);

        staleOpenRecords.forEach(TimeRecord::markIncomplete);
        timeRecordRepository.saveAll(staleOpenRecords);
        return staleOpenRecords;
    }

    private List<TimeRecordResponse> listRecords(
            final Long employeeId,
            final LocalDate from,
            final LocalDate to) {
        final LocalDate start = from != null ? from : LocalDate.now().minusMonths(1);
        final LocalDate end = to != null ? to : LocalDate.now();
        if (start.isAfter(end)) {
            return List.of();
        }
        return timeRecordRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(employeeId, start, end)
                .stream()
                .map(timeRecordMapper::toResponse)
                .toList();
    }

    private TimeRecord findRecordOrThrow(final Long recordId) {
        return timeRecordRepository.findById(recordId)
                .orElseThrow(() -> new TimeRecordNotFoundException(recordId));
    }

    private Employee resolveEmployeeByEmail(final String userEmail) {
        return employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmployeeProfileNotLinkedException(userEmail));
    }

    private void validateClockOutAfterClockIn(final Instant clockIn, final Instant clockOut) {
        if (!clockOut.isAfter(clockIn)) {
            throw new InvalidTimeRecordOperationException("Clock-out must be after clock-in");
        }
    }
}

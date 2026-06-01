package com.stepcore.business.time.service;

import com.stepcore.business.employee.domain.model.Employee;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.exception.DuplicateTimeRecordException;
import com.stepcore.business.exception.EmployeeNotFoundException;
import com.stepcore.business.exception.EmployeeProfileNotLinkedException;
import com.stepcore.business.exception.InvalidTimeRecordOperationException;
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

    private List<TimeRecordResponse> listRecords(
            final Long employeeId,
            final LocalDate from,
            final LocalDate to) {
        final LocalDate start = from != null ? from : LocalDate.now().minusMonths(1);
        final LocalDate end = to != null ? to : LocalDate.now();
        return timeRecordRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(employeeId, start, end)
                .stream()
                .map(timeRecordMapper::toResponse)
                .toList();
    }

    private Employee resolveEmployeeByEmail(final String userEmail) {
        return employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmployeeProfileNotLinkedException(userEmail));
    }
}

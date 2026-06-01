package com.stepcore.business.time.service;

import com.stepcore.business.employee.domain.model.Employee;
import com.stepcore.business.employee.domain.model.IdType;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.exception.DuplicateTimeRecordException;
import com.stepcore.business.exception.InvalidTimeRecordOperationException;
import com.stepcore.business.time.controller.dto.TimeRecordResponse;
import com.stepcore.business.time.controller.mapper.TimeRecordMapper;
import com.stepcore.business.time.domain.model.TimeRecord;
import com.stepcore.business.time.domain.model.TimeRecordStatus;
import com.stepcore.business.time.repository.TimeRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeRecordServiceTest {

    @Mock private TimeRecordRepository timeRecordRepository;
    @Mock private EmployeeRepository employeeRepository;

    private final TimeRecordMapper timeRecordMapper = new TimeRecordMapper();

    @InjectMocks private TimeRecordServiceImpl timeRecordService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        timeRecordService = new TimeRecordServiceImpl(
                timeRecordRepository, employeeRepository, timeRecordMapper);
        employee = Employee.builder()
                .withId(10L)
                .withTenantId(2L)
                .withFirstName("Ana")
                .withLastName("Employee")
                .withIdType(IdType.CC)
                .withIdNumber("123")
                .withEmail("employee@test.com")
                .withMonthlySalary(new BigDecimal("3000000"))
                .build();
    }

    @Test
    void shouldClockInWhenNoRecordExistsForToday() {
        when(employeeRepository.findByEmail("employee@test.com")).thenReturn(Optional.of(employee));
        when(timeRecordRepository.existsByEmployeeIdAndWorkDate(10L, LocalDate.now())).thenReturn(false);
        when(timeRecordRepository.save(any())).thenAnswer(inv -> {
            final TimeRecord record = inv.getArgument(0);
            return TimeRecord.builder()
                    .withId(1L)
                    .withEmployeeId(record.getEmployeeId())
                    .withWorkDate(record.getWorkDate())
                    .withClockIn(record.getClockIn())
                    .withStatus(record.getStatus())
                    .build();
        });

        final TimeRecordResponse response = timeRecordService.clockIn("employee@test.com");

        assertThat(response.status()).isEqualTo(TimeRecordStatus.OPEN);
        assertThat(response.employeeId()).isEqualTo(10L);
        verify(timeRecordRepository).save(any());
    }

    @Test
    void shouldRejectDuplicateClockIn() {
        when(employeeRepository.findByEmail("employee@test.com")).thenReturn(Optional.of(employee));
        when(timeRecordRepository.existsByEmployeeIdAndWorkDate(10L, LocalDate.now())).thenReturn(true);

        assertThatThrownBy(() -> timeRecordService.clockIn("employee@test.com"))
                .isInstanceOf(DuplicateTimeRecordException.class);
    }

    @Test
    void shouldClockOutOpenRecord() {
        final TimeRecord openRecord = TimeRecord.builder()
                .withId(1L)
                .withEmployeeId(10L)
                .withWorkDate(LocalDate.now())
                .withClockIn(Instant.now().minusSeconds(3600))
                .withStatus(TimeRecordStatus.OPEN)
                .build();

        when(employeeRepository.findByEmail("employee@test.com")).thenReturn(Optional.of(employee));
        when(timeRecordRepository.findByEmployeeIdAndWorkDate(10L, LocalDate.now()))
                .thenReturn(Optional.of(openRecord));
        when(timeRecordRepository.save(openRecord)).thenReturn(openRecord);

        final TimeRecordResponse response = timeRecordService.clockOut("employee@test.com");

        assertThat(response.status()).isEqualTo(TimeRecordStatus.CLOSED);
        assertThat(openRecord.getClockOut()).isNotNull();
    }

    @Test
    void shouldRejectClockOutWithoutClockIn() {
        when(employeeRepository.findByEmail("employee@test.com")).thenReturn(Optional.of(employee));
        when(timeRecordRepository.findByEmployeeIdAndWorkDate(10L, LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeRecordService.clockOut("employee@test.com"))
                .isInstanceOf(InvalidTimeRecordOperationException.class);
    }

    @Test
    void shouldRejectDuplicateClockOut() {
        final TimeRecord closedRecord = TimeRecord.builder()
                .withId(1L)
                .withEmployeeId(10L)
                .withWorkDate(LocalDate.now())
                .withClockIn(Instant.now().minusSeconds(7200))
                .withClockOut(Instant.now().minusSeconds(3600))
                .withStatus(TimeRecordStatus.CLOSED)
                .build();

        when(employeeRepository.findByEmail("employee@test.com")).thenReturn(Optional.of(employee));
        when(timeRecordRepository.findByEmployeeIdAndWorkDate(10L, LocalDate.now()))
                .thenReturn(Optional.of(closedRecord));

        assertThatThrownBy(() -> timeRecordService.clockOut("employee@test.com"))
                .isInstanceOf(DuplicateTimeRecordException.class);
    }
}

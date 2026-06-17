package com.stepcore.business.time.service;

import com.stepcore.business.employee.domain.model.Employee;
import com.stepcore.business.employee.domain.model.IdType;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.exception.CorrectionRequestNotPendingException;
import com.stepcore.business.exception.DuplicateCorrectionRequestException;
import com.stepcore.business.exception.InvalidTimeRecordOperationException;
import com.stepcore.business.notification.service.AdminNotificationService;
import com.stepcore.business.time.controller.dto.CorrectionRequestResponse;
import com.stepcore.business.time.controller.dto.CreateCorrectionRequestRequest;
import com.stepcore.business.time.controller.dto.DismissCorrectionRequestRequest;
import com.stepcore.business.time.domain.model.TimeCorrectionRequest;
import com.stepcore.business.time.domain.model.TimeCorrectionRequestStatus;
import com.stepcore.business.time.domain.model.TimeRecord;
import com.stepcore.business.time.domain.model.TimeRecordStatus;
import com.stepcore.business.time.repository.TimeCorrectionRequestRepository;
import com.stepcore.business.time.repository.TimeRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorrectionRequestServiceTest {

    @Mock private TimeCorrectionRequestRepository correctionRequestRepository;
    @Mock private TimeRecordRepository timeRecordRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AdminNotificationService adminNotificationService;
    @Mock private JdbcTemplate jdbcTemplate;

    private CorrectionRequestServiceImpl service;

    private Employee employee;
    private TimeRecord closedRecord;

    @BeforeEach
    void setUp() {
        service = new CorrectionRequestServiceImpl(
                correctionRequestRepository, timeRecordRepository, employeeRepository,
                adminNotificationService, jdbcTemplate);

        employee = Employee.builder()
                .withId(10L)
                .withTenantId(2L)
                .withFirstName("Ana")
                .withLastName("Lopez")
                .withIdType(IdType.CC)
                .withIdNumber("111")
                .withEmail("ana@test.com")
                .withMonthlySalary(new BigDecimal("3000000"))
                .withUserId(5L)
                .build();

        closedRecord = TimeRecord.builder()
                .withId(1L)
                .withTenantId(2L)
                .withEmployeeId(10L)
                .withWorkDate(LocalDate.of(2025, 6, 1))
                .withClockIn(Instant.parse("2025-06-01T08:00:00Z"))
                .withClockOut(Instant.parse("2025-06-01T17:00:00Z"))
                .withStatus(TimeRecordStatus.CLOSED)
                .build();
    }

    // ── submit ────────────────────────────────────────────────────────────────

    @Test
    void submit_createsRequestAndNotifiesAdmins() {
        when(employeeRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(employee));
        when(timeRecordRepository.findById(1L)).thenReturn(Optional.of(closedRecord));
        when(correctionRequestRepository.existsByTimeRecordIdAndStatus(1L, TimeCorrectionRequestStatus.PENDING))
                .thenReturn(false);
        when(correctionRequestRepository.save(any())).thenAnswer(inv -> {
            final TimeCorrectionRequest req = inv.getArgument(0);
            return TimeCorrectionRequest.builder()
                    .withId(99L)
                    .withTenantId(2L)
                    .withTimeRecordId(req.getTimeRecordId())
                    .withEmployeeId(req.getEmployeeId())
                    .withNote(req.getNote())
                    .withStatus(TimeCorrectionRequestStatus.PENDING)
                    .build();
        });

        final CorrectionRequestResponse response = service.submit(
                "ana@test.com", 1L, new CreateCorrectionRequestRequest("Wrong clock-out time"));

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.note()).isEqualTo("Wrong clock-out time");
        verify(adminNotificationService).saveCorrectionRequestNotification(anyString(), anyString());
    }

    @Test
    void submit_rejectsNonClosedRecord() {
        final TimeRecord openRecord = TimeRecord.builder()
                .withId(2L)
                .withTenantId(2L)
                .withEmployeeId(10L)
                .withWorkDate(LocalDate.now())
                .withClockIn(Instant.now())
                .withStatus(TimeRecordStatus.OPEN)
                .build();

        when(employeeRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(employee));
        when(timeRecordRepository.findById(2L)).thenReturn(Optional.of(openRecord));

        assertThatThrownBy(() -> service.submit(
                "ana@test.com", 2L, new CreateCorrectionRequestRequest("Note")))
                .isInstanceOf(InvalidTimeRecordOperationException.class);

        verify(correctionRequestRepository, never()).save(any());
    }

    @Test
    void submit_rejectsDuplicatePendingRequest() {
        when(employeeRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(employee));
        when(timeRecordRepository.findById(1L)).thenReturn(Optional.of(closedRecord));
        when(correctionRequestRepository.existsByTimeRecordIdAndStatus(1L, TimeCorrectionRequestStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> service.submit(
                "ana@test.com", 1L, new CreateCorrectionRequestRequest("Note")))
                .isInstanceOf(DuplicateCorrectionRequestException.class);

        verify(correctionRequestRepository, never()).save(any());
    }

    // ── autoResolve ──────────────────────────────────────────────────────────

    @Test
    void autoResolve_transitionsPendingRequestToResolved() {
        final TimeCorrectionRequest pendingRequest = TimeCorrectionRequest.builder()
                .withId(50L)
                .withTenantId(2L)
                .withTimeRecordId(1L)
                .withEmployeeId(10L)
                .withNote("Fix it")
                .withStatus(TimeCorrectionRequestStatus.PENDING)
                .build();

        when(correctionRequestRepository.findByTimeRecordIdAndStatus(1L, TimeCorrectionRequestStatus.PENDING))
                .thenReturn(Optional.of(pendingRequest));
        when(correctionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.autoResolve(1L);

        assertThat(pendingRequest.getStatus()).isEqualTo(TimeCorrectionRequestStatus.RESOLVED);
        verify(correctionRequestRepository).save(pendingRequest);
    }

    @Test
    void autoResolve_doesNothingWhenNoPendingRequest() {
        when(correctionRequestRepository.findByTimeRecordIdAndStatus(1L, TimeCorrectionRequestStatus.PENDING))
                .thenReturn(Optional.empty());

        service.autoResolve(1L);

        verify(correctionRequestRepository, never()).save(any());
    }

    // ── dismiss ───────────────────────────────────────────────────────────────

    @Test
    void dismiss_transitionsToDismissedWithReason() {
        final TimeCorrectionRequest pendingRequest = TimeCorrectionRequest.builder()
                .withId(50L)
                .withTenantId(2L)
                .withTimeRecordId(1L)
                .withEmployeeId(10L)
                .withNote("Fix it")
                .withStatus(TimeCorrectionRequestStatus.PENDING)
                .build();

        when(correctionRequestRepository.findById(50L)).thenReturn(Optional.of(pendingRequest));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString())).thenReturn(7L);
        when(correctionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(timeRecordRepository.findById(1L)).thenReturn(Optional.of(closedRecord));

        final CorrectionRequestResponse response = service.dismiss(
                "admin@test.com", 50L, new DismissCorrectionRequestRequest("No change needed"));

        assertThat(response.status()).isEqualTo("DISMISSED");
        assertThat(pendingRequest.getResolutionNote()).isEqualTo("No change needed");
    }

    @Test
    void dismiss_rejectsAlreadyResolvedRequest() {
        final TimeCorrectionRequest resolvedRequest = TimeCorrectionRequest.builder()
                .withId(51L)
                .withTenantId(2L)
                .withTimeRecordId(1L)
                .withEmployeeId(10L)
                .withNote("Fix it")
                .withStatus(TimeCorrectionRequestStatus.RESOLVED)
                .build();

        when(correctionRequestRepository.findById(51L)).thenReturn(Optional.of(resolvedRequest));

        assertThatThrownBy(() -> service.dismiss(
                "admin@test.com", 51L, new DismissCorrectionRequestRequest("Too late")))
                .isInstanceOf(CorrectionRequestNotPendingException.class);
    }
}

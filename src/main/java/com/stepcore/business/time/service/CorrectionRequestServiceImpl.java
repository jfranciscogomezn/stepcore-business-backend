package com.stepcore.business.time.service;

import com.stepcore.business.employee.domain.model.Employee;
import com.stepcore.business.employee.repository.EmployeeRepository;
import com.stepcore.business.exception.CorrectionRequestNotFoundException;
import com.stepcore.business.exception.CorrectionRequestNotPendingException;
import com.stepcore.business.exception.DuplicateCorrectionRequestException;
import com.stepcore.business.exception.EmployeeNotFoundException;
import com.stepcore.business.exception.EmployeeProfileNotLinkedException;
import com.stepcore.business.exception.InvalidTimeRecordOperationException;
import com.stepcore.business.exception.TimeRecordNotFoundException;
import com.stepcore.business.notification.domain.model.AdminNotification;
import com.stepcore.business.notification.service.AdminNotificationService;
import com.stepcore.business.tenant.TenantContext;
import com.stepcore.business.time.controller.dto.CorrectionRequestResponse;
import com.stepcore.business.time.controller.dto.CreateCorrectionRequestRequest;
import com.stepcore.business.time.controller.dto.DismissCorrectionRequestRequest;
import com.stepcore.business.time.domain.model.TimeCorrectionRequest;
import com.stepcore.business.time.domain.model.TimeCorrectionRequestStatus;
import com.stepcore.business.time.domain.model.TimeRecord;
import com.stepcore.business.time.repository.TimeCorrectionRequestRepository;
import com.stepcore.business.time.repository.TimeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CorrectionRequestServiceImpl implements CorrectionRequestService {

    private static final String ADMIN_USER_ID_SQL = """
            SELECT u.id
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.tenant_id = ?
              AND u.enabled = TRUE
              AND r.name = 'ADMIN'
            """;

    private final TimeCorrectionRequestRepository correctionRequestRepository;
    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final AdminNotificationService adminNotificationService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public CorrectionRequestResponse submit(
            final String actorEmail,
            final Long timeRecordId,
            final CreateCorrectionRequestRequest request) {
        final Employee employee = resolveEmployeeByEmail(actorEmail);
        final TimeRecord record = findRecordOrThrow(timeRecordId);

        if (!record.isClosed()) {
            throw new InvalidTimeRecordOperationException(
                    "Correction requests can only be submitted for CLOSED records");
        }
        if (!record.getEmployeeId().equals(employee.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cannot request correction for another employee's record");
        }
        if (correctionRequestRepository.existsByTimeRecordIdAndStatus(
                timeRecordId, TimeCorrectionRequestStatus.PENDING)) {
            throw new DuplicateCorrectionRequestException(timeRecordId);
        }

        final TimeCorrectionRequest correctionRequest = TimeCorrectionRequest.builder()
                .withTimeRecordId(timeRecordId)
                .withEmployeeId(employee.getId())
                .withNote(request.note().trim())
                .withStatus(TimeCorrectionRequestStatus.PENDING)
                .build();

        final TimeCorrectionRequest saved = correctionRequestRepository.save(correctionRequest);

        notifyAdminsOfNewRequest(employee, record.getWorkDate(), request.note().trim());

        log.info("[CorrectionRequestServiceImpl] - SUBMIT: requestId={} employeeId={} timeRecordId={}",
                saved.getId(), employee.getId(), timeRecordId);

        return toResponse(saved, employee.getFirstName() + " " + employee.getLastName(), record.getWorkDate());
    }

    @Override
    public CorrectionRequestResponse dismiss(
            final String actorEmail,
            final Long requestId,
            final DismissCorrectionRequestRequest request) {
        final TimeCorrectionRequest correctionRequest = findRequestOrThrow(requestId);

        if (!correctionRequest.isPending()) {
            throw new CorrectionRequestNotPendingException(requestId);
        }

        final Long actorUserId = resolveUserIdByEmail(actorEmail);
        correctionRequest.dismiss(actorUserId, request.dismissalReason().trim());
        final TimeCorrectionRequest saved = correctionRequestRepository.save(correctionRequest);

        log.info("[CorrectionRequestServiceImpl] - DISMISS: requestId={} actorEmail={}",
                requestId, actorEmail);

        final Employee employee = employeeRepository.findById(correctionRequest.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(correctionRequest.getEmployeeId()));
        final TimeRecord record = findRecordOrThrow(correctionRequest.getTimeRecordId());

        return toResponse(saved, employee.getFirstName() + " " + employee.getLastName(), record.getWorkDate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorrectionRequestResponse> listPending() {
        final Long tenantId = TenantContext.getTenantIdOrDefault();
        final List<TimeCorrectionRequest> requests = correctionRequestRepository
                .findByTenantIdAndStatusOrderByCreatedAtAsc(tenantId, TimeCorrectionRequestStatus.PENDING);

        return requests.stream()
                .map(req -> {
                    final Employee emp = employeeRepository.findById(req.getEmployeeId())
                            .orElse(null);
                    final String name = emp != null
                            ? emp.getFirstName() + " " + emp.getLastName()
                            : "Employee #" + req.getEmployeeId();
                    final TimeRecord record = timeRecordRepository.findById(req.getTimeRecordId())
                            .orElse(null);
                    final LocalDate workDate = record != null ? record.getWorkDate() : null;
                    return toResponse(req, name, workDate);
                })
                .toList();
    }

    @Override
    public void autoResolve(final Long timeRecordId) {
        correctionRequestRepository
                .findByTimeRecordIdAndStatus(timeRecordId, TimeCorrectionRequestStatus.PENDING)
                .ifPresent(req -> {
                    req.resolve();
                    correctionRequestRepository.save(req);
                    log.info("[CorrectionRequestServiceImpl] - AUTO_RESOLVE: requestId={} timeRecordId={}",
                            req.getId(), timeRecordId);
                });
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void notifyAdminsOfNewRequest(
            final Employee employee,
            final LocalDate workDate,
            final String note) {
        final String employeeName = employee.getFirstName() + " " + employee.getLastName();
        final String title = "Correction request submitted by " + employeeName;
        final String message = employeeName + " requested a correction for " + workDate + ": " + note;
        adminNotificationService.saveCorrectionRequestNotification(title, message);
    }

    private Long resolveUserIdByEmail(final String email) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = ? LIMIT 1",
                    Long.class,
                    email);
        } catch (final Exception ex) {
            log.debug("[CorrectionRequestServiceImpl] - Could not resolve userId for {}: {}", email, ex.getMessage());
            return null;
        }
    }

    private Employee resolveEmployeeByEmail(final String userEmail) {
        return employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EmployeeProfileNotLinkedException(userEmail));
    }

    private TimeRecord findRecordOrThrow(final Long recordId) {
        return timeRecordRepository.findById(recordId)
                .orElseThrow(() -> new TimeRecordNotFoundException(recordId));
    }

    private TimeCorrectionRequest findRequestOrThrow(final Long requestId) {
        return correctionRequestRepository.findById(requestId)
                .orElseThrow(() -> new CorrectionRequestNotFoundException(requestId));
    }

    private CorrectionRequestResponse toResponse(
            final TimeCorrectionRequest req,
            final String employeeName,
            final LocalDate recordDate) {
        return new CorrectionRequestResponse(
                req.getId(),
                req.getTimeRecordId(),
                req.getEmployeeId(),
                employeeName,
                recordDate,
                req.getNote(),
                req.getStatus().name(),
                req.getResolutionNote(),
                req.getCreatedAt(),
                req.getResolvedAt());
    }
}

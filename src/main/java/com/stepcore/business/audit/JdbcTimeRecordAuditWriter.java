package com.stepcore.business.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepcore.business.audit.model.TimeRecordAuditAction;
import com.stepcore.business.audit.model.TimeRecordAuditSnapshot;
import com.stepcore.business.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JdbcTimeRecordAuditWriter implements TimeRecordAuditWriter {

    private static final String ENTITY_TYPE = "TIME_RECORD";

    private static final String INSERT_SQL = """
            INSERT INTO audit_logs (tenant_id, user_id, action, entity_type, entity_id, old_value, new_value, details)
            VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)
            """;

    private static final String USER_LOOKUP_SQL = """
            SELECT id FROM users WHERE email = ? AND tenant_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void logChange(
            final String actorEmail,
            final TimeRecordAuditAction action,
            final Long timeRecordId,
            final TimeRecordAuditSnapshot before,
            final TimeRecordAuditSnapshot after,
            final String correctionReason) {
        final long tenantId = TenantContext.getTenantIdOrDefault();
        final Long userId = resolveUserId(actorEmail, tenantId).orElse(null);
        final String details = buildDetails(actorEmail, correctionReason);

        try {
            jdbcTemplate.update(
                    INSERT_SQL,
                    tenantId,
                    userId,
                    action.name(),
                    ENTITY_TYPE,
                    String.valueOf(timeRecordId),
                    toJson(before),
                    toJson(after),
                    details);
            log.info(
                    "[JdbcTimeRecordAuditWriter] - LOG: action={} recordId={} actor={}",
                    action,
                    timeRecordId,
                    actorEmail);
        } catch (final RuntimeException exception) {
            log.error(
                    "[JdbcTimeRecordAuditWriter] - FAILED: action={} recordId={} actor={}",
                    action,
                    timeRecordId,
                    actorEmail,
                    exception);
            throw exception;
        }
    }

    private Optional<Long> resolveUserId(final String actorEmail, final long tenantId) {
        try {
            return jdbcTemplate.query(
                            USER_LOOKUP_SQL,
                            (resultSet, rowNum) -> resultSet.getLong("id"),
                            actorEmail,
                            tenantId)
                    .stream()
                    .findFirst();
        } catch (final RuntimeException exception) {
            log.debug("[JdbcTimeRecordAuditWriter] - user lookup skipped: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    private String buildDetails(final String actorEmail, final String correctionReason) {
        final StringBuilder builder = new StringBuilder("actorEmail=").append(actorEmail);
        if (correctionReason != null && !correctionReason.isBlank()) {
            builder.append("; correctionReason=").append(correctionReason);
        }
        return builder.toString();
    }

    private String toJson(final TimeRecordAuditSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("employeeId", snapshot.employeeId());
        payload.put("workDate", snapshot.workDate() != null ? snapshot.workDate().toString() : null);
        payload.put("clockIn", snapshot.clockIn() != null ? snapshot.clockIn().toString() : null);
        payload.put("clockOut", snapshot.clockOut() != null ? snapshot.clockOut().toString() : null);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (final JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize audit snapshot", exception);
        }
    }
}

package com.stepcore.business.audit.service;

import com.stepcore.business.audit.controller.dto.TimeRecordAuditEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeRecordAuditService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;
    private static final Pattern ACTOR_EMAIL = Pattern.compile("actorEmail=([^;]+)");
    private static final Pattern CORRECTION_REASON = Pattern.compile("correctionReason=([^;]+)");

    private static final String BASE_SELECT = """
            SELECT id, user_id, action, entity_id, old_value::text, new_value::text, details, created_at
            FROM audit_logs
            WHERE entity_type = 'TIME_RECORD'
            """;

    private final JdbcTemplate jdbcTemplate;

    public List<TimeRecordAuditEntryResponse> list(final TimeRecordAuditFilter filter) {
        final StringBuilder sql = new StringBuilder(BASE_SELECT);
        final List<Object> params = new ArrayList<>();

        if (filter.fromDate() != null) {
            sql.append(" AND created_at >= ?");
            params.add(filter.fromDate().atStartOfDay());
        }
        if (filter.toDate() != null) {
            sql.append(" AND created_at < ?");
            params.add(filter.toDate().plusDays(1).atStartOfDay());
        }
        if (filter.userId() != null) {
            sql.append(" AND user_id = ?");
            params.add(filter.userId());
        }
        if (filter.employeeId() != null) {
            sql.append("""
                     AND (
                       (new_value::jsonb->>'employeeId')::bigint = ?
                       OR (old_value::jsonb->>'employeeId')::bigint = ?
                     )
                    """);
            params.add(filter.employeeId());
            params.add(filter.employeeId());
        }

        sql.append(" ORDER BY created_at DESC LIMIT ?");
        params.add(resolveLimit(filter.limit()));

        return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
    }

    private int resolveLimit(final int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private TimeRecordAuditEntryResponse mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
        final String details = resultSet.getString("details");
        final Long userId = resultSet.getObject("user_id") != null ? resultSet.getLong("user_id") : null;
        return new TimeRecordAuditEntryResponse(
                resultSet.getLong("id"),
                resultSet.getString("action"),
                resultSet.getString("entity_id"),
                userId,
                extractActorEmail(details),
                resultSet.getString("old_value"),
                resultSet.getString("new_value"),
                extractCorrectionReason(details),
                details,
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }

    private String extractActorEmail(final String details) {
        if (details == null) {
            return null;
        }
        final Matcher matcher = ACTOR_EMAIL.matcher(details);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String extractCorrectionReason(final String details) {
        if (details == null) {
            return null;
        }
        final Matcher matcher = CORRECTION_REASON.matcher(details);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
}

package com.stepcore.business.audit.service;

import com.stepcore.business.audit.controller.dto.TimeRecordAuditEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeRecordAuditService {

    private static final int DEFAULT_LIMIT = 50;
    private static final Pattern ACTOR_EMAIL = Pattern.compile("actorEmail=([^;]+)");

    private static final String LIST_SQL = """
            SELECT id, action, entity_id, old_value::text, new_value::text, details, created_at
            FROM audit_logs
            WHERE entity_type = 'TIME_RECORD'
            ORDER BY created_at DESC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public List<TimeRecordAuditEntryResponse> listRecent(final int limit) {
        final int effectiveLimit = limit > 0 ? Math.min(limit, DEFAULT_LIMIT) : DEFAULT_LIMIT;
        return jdbcTemplate.query(LIST_SQL, this::mapRow, effectiveLimit);
    }

    private TimeRecordAuditEntryResponse mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
        final String details = resultSet.getString("details");
        return new TimeRecordAuditEntryResponse(
                resultSet.getLong("id"),
                resultSet.getString("action"),
                resultSet.getString("entity_id"),
                extractActorEmail(details),
                resultSet.getString("old_value"),
                resultSet.getString("new_value"),
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
}

package com.stepcore.business.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepcore.business.audit.model.TimeRecordAuditAction;
import com.stepcore.business.audit.model.TimeRecordAuditSnapshot;
import com.stepcore.business.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcTimeRecordAuditWriterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private JdbcTimeRecordAuditWriter writer;

    @BeforeEach
    void setUp() {
        writer = new JdbcTimeRecordAuditWriter(jdbcTemplate, objectMapper);
        TenantContext.setTenantId(2L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldInsertAuditRowWithJsonPayload() {
        when(jdbcTemplate.query(any(String.class), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(java.util.List.of());

        final TimeRecordAuditSnapshot before = new TimeRecordAuditSnapshot(
                10L,
                LocalDate.of(2026, 5, 28),
                Instant.parse("2026-05-28T08:00:00Z"),
                null);
        final TimeRecordAuditSnapshot after = new TimeRecordAuditSnapshot(
                10L,
                LocalDate.of(2026, 5, 28),
                Instant.parse("2026-05-28T08:00:00Z"),
                Instant.parse("2026-05-28T17:00:00Z"));

        writer.logChange(
                "admin@test.com",
                TimeRecordAuditAction.TIME_RECORD_RESOLVE_INCOMPLETE,
                6L,
                before,
                after,
                "Forgot to clock out");

        verify(jdbcTemplate).update(
                any(String.class),
                eq(2L),
                eq(null),
                eq("TIME_RECORD_RESOLVE_INCOMPLETE"),
                eq("TIME_RECORD"),
                eq("6"),
                org.mockito.ArgumentMatchers.<String>argThat(value -> value.contains("employeeId")),
                org.mockito.ArgumentMatchers.<String>argThat(value -> value.contains("clockOut")),
                org.mockito.ArgumentMatchers.<String>argThat(value -> value.contains("actorEmail=admin@test.com")));
    }
}

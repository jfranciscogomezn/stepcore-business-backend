package com.stepcore.business.notification.service;

import com.stepcore.business.tenant.TenantContext;
import com.stepcore.business.time.domain.model.TimeRecord;
import com.stepcore.business.time.notification.IncompleteTimeRecordNotificationService;
import com.stepcore.business.time.service.TimeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncompleteTimeRecordJobService {

    private static final String ACTIVE_TENANTS_SQL = "SELECT id FROM tenants WHERE status = 'ACTIVE'";

    private final JdbcTemplate jdbcTemplate;
    private final TimeRecordService timeRecordService;
    private final IncompleteTimeRecordNotificationService notificationService;

    public void flagStaleRecordsAndNotify() {
        for (final Long tenantId : resolveActiveTenantIds()) {
            processTenant(tenantId);
        }
    }

    @Transactional
    public void processTenant(final Long tenantId) {
        TenantContext.setTenantId(tenantId);
        try {
            final List<TimeRecord> flagged = timeRecordService.flagStaleOpenRecordsAsIncomplete();
            if (!flagged.isEmpty()) {
                log.info(
                        "[IncompleteTimeRecordJobService] - FLAGGED: tenantId={} records={}",
                        tenantId,
                        flagged.size());
                notificationService.notifyAdminsOfIncompleteRecords(flagged);
            }
        } finally {
            TenantContext.clear();
        }
    }

    private List<Long> resolveActiveTenantIds() {
        try {
            final List<Long> tenantIds = jdbcTemplate.queryForList(ACTIVE_TENANTS_SQL, Long.class);
            if (!tenantIds.isEmpty()) {
                return tenantIds;
            }
        } catch (final DataAccessException exception) {
            log.debug(
                    "[IncompleteTimeRecordJobService] - tenants table unavailable: {}",
                    exception.getMessage());
        }
        return List.of(TenantContext.LEGACY_TENANT_ID);
    }
}

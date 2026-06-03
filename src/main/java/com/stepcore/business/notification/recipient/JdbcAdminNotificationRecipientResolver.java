package com.stepcore.business.notification.recipient;

import com.stepcore.business.notification.config.NotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JdbcAdminNotificationRecipientResolver implements AdminNotificationRecipientResolver {

    private static final String ADMIN_EMAILS_SQL = """
            SELECT u.email
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.tenant_id = ?
              AND u.enabled = TRUE
              AND r.name = 'ADMIN'
            """;

    private final JdbcTemplate jdbcTemplate;
    private final NotificationProperties notificationProperties;

    @Override
    public List<String> resolveAdminEmails(final long tenantId) {
        if (notificationProperties.isJdbcAdminLookupEnabled()) {
            try {
                final List<String> emails = jdbcTemplate.query(
                        ADMIN_EMAILS_SQL,
                        (resultSet, rowNum) -> resultSet.getString("email"),
                        tenantId);
                if (!emails.isEmpty()) {
                    return emails;
                }
            } catch (final DataAccessException exception) {
                log.debug(
                        "[JdbcAdminNotificationRecipientResolver] - JDBC lookup unavailable: {}",
                        exception.getMessage());
            }
        }
        return List.copyOf(notificationProperties.getFallbackAdminEmails());
    }
}

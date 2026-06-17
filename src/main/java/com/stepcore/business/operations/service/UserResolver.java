package com.stepcore.business.operations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Resolves IAM user IDs by email using the shared `users` table.
 * Returns null gracefully when the table is unavailable (e.g., in unit tests).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserResolver {

    private final JdbcTemplate jdbcTemplate;

    public Long resolveByEmail(final String email) {
        if (email == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = ? LIMIT 1",
                    Long.class,
                    email);
        } catch (final Exception ex) {
            log.debug("[UserResolver] - Could not resolve userId for {}: {}", email, ex.getMessage());
            return null;
        }
    }
}

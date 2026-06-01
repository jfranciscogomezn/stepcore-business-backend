package com.stepcore.business.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class JwtTestSupport {

    public static final String SECRET =
            "test-secret-key-for-unit-tests-only-not-for-production-use";

    private static final List<String> ADMIN_PERMISSIONS = List.of(
            "ROLE_MANAGEMENT",
            "USER_MANAGEMENT",
            "PAYROLL_CONFIG",
            "EMPLOYEE_CONFIG",
            "TIME_RECORDS_ADMIN",
            "REPORTS",
            "MY_TIME",
            "MY_PROFILE");

    private static final List<String> EMPLOYEE_PERMISSIONS = List.of("MY_TIME", "MY_PROFILE");

    private JwtTestSupport() {
    }

    public static String adminToken(final Long tenantId) {
        return token("admin@test.com", tenantId, List.of("ADMIN"), ADMIN_PERMISSIONS);
    }

    public static String employeeToken(final Long tenantId) {
        return token("employee@test.com", tenantId, List.of("EMPLOYEE"), EMPLOYEE_PERMISSIONS);
    }

    public static String token(
            final String email,
            final Long tenantId,
            final List<String> roles,
            final List<String> permissions) {
        final long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .claims(Map.of(
                        "tenant_id", tenantId.toString(),
                        "tenant_slug", "test-tenant",
                        "tenant_plan", "STANDARD",
                        "roles", roles,
                        "permissions", permissions))
                .issuedAt(new Date(now))
                .expiration(new Date(now + 3_600_000L))
                .signWith(signingKey())
                .compact();
    }

    private static SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}

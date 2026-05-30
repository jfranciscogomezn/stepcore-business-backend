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

    private JwtTestSupport() {
    }

    public static String adminToken(final Long tenantId) {
        return token("admin@test.com", tenantId, List.of("ADMIN"));
    }

    public static String employeeToken(final Long tenantId) {
        return token("employee@test.com", tenantId, List.of("EMPLOYEE"));
    }

    public static String token(final String email, final Long tenantId, final List<String> roles) {
        final long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .claims(Map.of(
                        "tenant_id", tenantId.toString(),
                        "tenant_slug", "test-tenant",
                        "tenant_plan", "STANDARD",
                        "roles", roles))
                .issuedAt(new Date(now))
                .expiration(new Date(now + 3_600_000L))
                .signWith(signingKey())
                .compact();
    }

    private static SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}

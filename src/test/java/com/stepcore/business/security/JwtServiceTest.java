package com.stepcore.business.security;

import com.stepcore.business.config.JwtProperties;
import com.stepcore.business.support.JwtTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(JwtTestSupport.SECRET));
    }

    @Test
    void shouldValidateTokenSignedBySecurityService() {
        final String token = JwtTestSupport.adminToken(2L);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void shouldRejectTokenWithWrongSecret() {
        final JwtService other = new JwtService(new JwtProperties("another-secret-that-is-long-enough-for-hmac-sha256"));
        final String token = JwtTestSupport.adminToken(2L);
        assertThat(other.isTokenValid(token)).isFalse();
    }

    @Test
    void shouldExtractEmailTenantAndRoles() {
        final String token = JwtTestSupport.adminToken(42L);
        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@test.com");
        assertThat(jwtService.extractTenantId(token)).isEqualTo(42L);
        assertThat(jwtService.extractRoles(token)).containsExactly("ADMIN");
    }

    @Test
    void shouldReturnEmptyRolesWhenClaimMissing() {
        final long now = System.currentTimeMillis();
        final String token = io.jsonwebtoken.Jwts.builder()
                .subject("user@test.com")
                .issuedAt(new java.util.Date(now))
                .expiration(new java.util.Date(now + 3_600_000L))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        JwtTestSupport.SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
        assertThat(jwtService.extractRoles(token)).isEmpty();
    }

    @Test
    void shouldThrowWhenTokenIsMalformed() {
        assertThatThrownBy(() -> jwtService.extractEmail("not.a.valid.token"))
                .isInstanceOf(Exception.class);
    }
}

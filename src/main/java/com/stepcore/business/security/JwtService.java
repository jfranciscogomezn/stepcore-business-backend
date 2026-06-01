package com.stepcore.business.security;

import com.stepcore.business.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    public static final String CLAIM_TENANT_ID = "tenant_id";
    public static final String CLAIM_TENANT_SLUG = "tenant_slug";
    public static final String CLAIM_TENANT_PLAN = "tenant_plan";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "permissions";

    private final JwtProperties jwtProperties;

    public boolean isTokenValid(final String token) {
        try {
            final Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JwtService] - TOKEN_VALIDATION: invalid token: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractTenantId(final String token) {
        final String raw = extractClaim(token, claims -> claims.get(CLAIM_TENANT_ID, String.class));
        return raw == null ? null : Long.valueOf(raw);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(final String token) {
        return extractStringListClaim(token, CLAIM_ROLES);
    }

    public List<String> extractPermissions(final String token) {
        return extractStringListClaim(token, CLAIM_PERMISSIONS);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractStringListClaim(final String token, final String claimName) {
        final Object raw = extractClaim(token, claims -> claims.get(claimName));
        if (raw == null) {
            return Collections.emptyList();
        }
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of(String.valueOf(raw));
    }

    private <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(parseClaims(token));
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}

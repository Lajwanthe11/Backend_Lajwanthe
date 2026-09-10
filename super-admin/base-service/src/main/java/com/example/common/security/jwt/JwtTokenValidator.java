package com.example.common.security.jwt;

import com.example.common.tenant.TenantContext;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Shared JWT token validator — available to any microservice via common-lib.
 *
 * Performs ONLY validation and claim extraction. No token generation.
 * Relies on the same shared {@code app.jwt.secret} that auth-service uses to sign tokens.
 *
 * Services: configure {@code app.jwt.secret} (via JWT_SECRET env var) to the same value as auth-service.
 */
@Component
public class JwtTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ---------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException ex) {
            log.error("Invalid JWT signature or format: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Claim Extraction
    // ---------------------------------------------------------------

    public String getUsernameFromJWT(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRolesFromJWT(String token) {
        return parseClaims(token).get("roles", String.class);
    }

    public String getTenantIdFromJWT(String token) {
        String tenantId = parseClaims(token).get("tenantId", String.class);
        return (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.DEFAULT_TENANT_ID;
    }
}

package com.example.auth.security.jwt;

import com.example.auth.security.user.UserPrincipal;
import com.example.common.tenant.TenantContext;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility for generating, parsing, and validating JSON Web Tokens (JWT) with Multi-Tenancy support.
 * This component lives only in the auth-service; the base-service uses JwtTokenValidator instead.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /** Claim shared by every token of one login session; links logout and refresh to its Login History record. */
    public static final String SESSION_ID_CLAIM = "sid";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationInMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ---------------------------------------------------------------
    // Token Generation
    // ---------------------------------------------------------------

    /** Generate an access token from a Spring Security Authentication object. */
    public String generateAccessToken(Authentication authentication) {
        return generateAccessToken(authentication, null);
    }

    public String generateAccessToken(Authentication authentication, String sessionId) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String tenantId = (principal instanceof UserPrincipal userPrincipal)
                ? userPrincipal.getTenantId()
                : TenantContext.getTenantId();

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return generateToken(principal.getUsername(), roles, tenantId, sessionId, jwtExpirationInMs);
    }

    /** Generate an access token from raw username/roles (used by OAuth2 success handler). */
    public String generateAccessToken(String username, String roles) {
        return generateAccessToken(username, roles, TenantContext.getTenantId());
    }

    public String generateAccessToken(String username, String roles, String tenantId) {
        return generateAccessToken(username, roles, tenantId, null);
    }

    public String generateAccessToken(String username, String roles, String tenantId, String sessionId) {
        return generateToken(username, roles,
                tenantId != null ? tenantId : TenantContext.getTenantId(),
                sessionId, jwtExpirationInMs);
    }

    /** Generate a refresh token (no roles embedded — minimal claims). */
    public String generateRefreshToken(String username) {
        return generateRefreshToken(username, TenantContext.getTenantId());
    }

    public String generateRefreshToken(String username, String tenantId) {
        return generateRefreshToken(username, tenantId, null);
    }

    public String generateRefreshToken(String username, String tenantId, String sessionId) {
        return generateToken(username, null,
                tenantId != null ? tenantId : TenantContext.getTenantId(),
                sessionId, refreshExpirationInMs);
    }

    private String generateToken(String username, String roles, String tenantId,
                                 String sessionId, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        Map<String, Object> claims = new HashMap<>();
        if (roles != null) {
            claims.put("roles", roles);
        }
        claims.put("tenantId", (tenantId != null && !tenantId.isBlank())
                ? tenantId : TenantContext.DEFAULT_TENANT_ID);
        if (sessionId != null) {
            claims.put(SESSION_ID_CLAIM, sessionId);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ---------------------------------------------------------------
    // Token Parsing
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

    public Date getExpiryFromJWT(String token) {
        return parseClaims(token).getExpiration();
    }

    /** The login-session id, or null for tokens issued without one. */
    public String getSessionIdFromJWT(String token) {
        return parseClaims(token).get(SESSION_ID_CLAIM, String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ---------------------------------------------------------------
    // Token Validation
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
}
package com.example.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory denylist for revoked JWT access tokens.
 *
 * When a user calls POST /auth/logout, their current access token is added here.
 * The {@link com.example.auth.security.jwt.JwtAuthenticationFilter} checks this
 * denylist on every authenticated request and rejects revoked tokens.
 *
 * Expired entries are lazily evicted during {@link #isRevoked(String)} calls.
 *
 * NOTE: This is an in-memory implementation, which means revoked tokens are
 * forgotten on service restart. For production, back this with Redis or a DB table.
 * TODO: Replace with Redis-backed denylist for distributed deployments.
 */
@Service
public class TokenDenylistService {

    private static final Logger log = LoggerFactory.getLogger(TokenDenylistService.class);

    /** Maps raw JWT string → its expiry time. */
    private final Map<String, Date> denylist = new ConcurrentHashMap<>();

    /**
     * Revoke a token by adding it to the denylist until it naturally expires.
     *
     * @param token  The raw JWT string to revoke.
     * @param expiry The token's own expiry date (used for lazy eviction).
     */
    public void revokeToken(String token, Date expiry) {
        denylist.put(token, expiry);
        log.debug("Token revoked, denylist size: {}", denylist.size());
    }

    /**
     * Check whether a token has been revoked.
     * Expired entries are lazily cleaned up during this check.
     *
     * @param token The raw JWT string to check.
     * @return {@code true} if the token is in the denylist (and not yet naturally expired).
     */
    public boolean isRevoked(String token) {
        Date expiry = denylist.get(token);
        if (expiry == null) {
            return false;
        }
        // Lazily evict if the token has naturally expired — the JWT validator
        // would reject it anyway, so no security value in keeping it.
        if (expiry.before(new Date())) {
            denylist.remove(token);
            return false;
        }
        return true;
    }
}

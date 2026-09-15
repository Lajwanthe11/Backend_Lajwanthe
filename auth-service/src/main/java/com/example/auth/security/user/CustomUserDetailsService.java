package com.example.auth.security.user;

import com.example.common.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tenant-aware UserDetailsService.
 * Out of the box, uses an in-memory ConcurrentHashMap for storage.
 * In production, replace the store with a tenant-aware UserRepository backed by a database.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /** In-memory user store keyed as "tenantId:username" for multi-tenant lookup. */
    private final Map<String, UserPrincipal> users = new ConcurrentHashMap<>();

    // --- Account lockout config (additive, externalized) ---
    @Value("${app.security.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.security.lockout.duration-minutes:15}")
    private int lockDurationMinutes;

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        // Seed default admin and user for out-of-the-box testing
        registerUser("admin", "admin@example.com", passwordEncoder.encode("admin123"),
                List.of("ROLE_ADMIN", "ROLE_USER"), TenantContext.DEFAULT_TENANT_ID);
        registerUser("user", "user@example.com", passwordEncoder.encode("user123"),
                List.of("ROLE_USER"), TenantContext.DEFAULT_TENANT_ID);
    }

    private String buildUserKey(String username, String tenantId) {
        String activeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.DEFAULT_TENANT_ID;
        return activeTenant + ":" + username;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String currentTenant = TenantContext.getTenantId();

        // Attempt lookup in current tenant first
        UserPrincipal user = users.get(buildUserKey(username, currentTenant));

        // Fallback to default tenant
        if (user == null) {
            user = users.get(buildUserKey(username, TenantContext.DEFAULT_TENANT_ID));
        }

        // Direct username lookup (for backward-compat seeded users)
        if (user == null) {
            user = users.get(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException(
                    String.format("User not found with username '%s' in tenant '%s'", username, currentTenant));
        }
        return user;
    }

    public void registerUser(String username, String email, String encodedPassword, List<String> roles) {
        registerUser(username, email, encodedPassword, roles, TenantContext.getTenantId());
    }

    public void registerUser(String username, String email, String encodedPassword, List<String> roles, String tenantId) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();
        UserPrincipal principal = UserPrincipal.create(username, email, encodedPassword, roles, effectiveTenant);
        users.put(buildUserKey(username, effectiveTenant), principal);
        users.put(username, principal); // Direct fallback key
    }

    public boolean existsByUsername(String username) {
        return existsByUsernameAndTenant(username, TenantContext.getTenantId());
    }

    public boolean existsByUsernameAndTenant(String username, String tenantId) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();
        return users.containsKey(buildUserKey(username, effectiveTenant)) || users.containsKey(username);
    }

    /**
     * Update the stored password for an existing user.
     * The caller is responsible for encoding the password before passing it here.
     *
     * @param username       the user whose password should be updated.
     * @param tenantId       the tenant the user belongs to.
     * @param encodedPassword the BCrypt-encoded new password.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the user is not found.
     */
    public void updatePassword(String username, String tenantId, String encodedPassword) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();
        String key = buildUserKey(username, effectiveTenant);
        UserPrincipal existing = users.get(key);
        if (existing == null) {
            existing = users.get(username);
        }
        if (existing == null) {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
                    String.format("User '%s' not found in tenant '%s'", username, effectiveTenant));
        }
        UserPrincipal updated = UserPrincipal.create(
                existing.getUsername(),
                existing.getEmail(),
                encodedPassword,
                existing.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .collect(java.util.stream.Collectors.toList()),
                effectiveTenant
        );
        users.put(key, updated);
        users.put(username, updated); // Keep direct-fallback key in sync
    }

    // ---------------------------------------------------------------
    // Account Lockout support (additive)
    // ---------------------------------------------------------------

    /**
     * Increment the failed-login counter for a user. If the counter reaches
     * maxAttempts, the account is locked for lockDurationMinutes.
     */
    public void incrementFailedAttempts(String username, String tenantId) {
        UserPrincipal user = resolveUser(username, tenantId);
        if (user == null) {
            return;
        }

        // Previous lock already expired na, fresh start pannu
        if (user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())) {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
        }

        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        if (attempts >= maxAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
        }
    }

    /**
     * Reset the failed-login counter and unlock the account after a successful login.
     */
    public void resetFailedAttempts(String username, String tenantId) {
        UserPrincipal user = resolveUser(username, tenantId);
        if (user == null) {
            return;
        }
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
    }

    /**
     * Look up the same UserPrincipal instance stored in the map (current tenant,
     * default tenant, then direct-username fallback) so mutations affect the stored object.
     */
    private UserPrincipal resolveUser(String username, String tenantId) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();

        UserPrincipal user = users.get(buildUserKey(username, effectiveTenant));
        if (user == null) {
            user = users.get(buildUserKey(username, TenantContext.DEFAULT_TENANT_ID));
        }
        if (user == null) {
            user = users.get(username);
        }
        return user;
    }

    public LocalDateTime getLockedUntil(String username, String tenantId) {
        UserPrincipal user = resolveUser(username, tenantId);
        return user != null ? user.getLockedUntil() : null;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getLockDurationMinutes() {
        return lockDurationMinutes;
    }
}
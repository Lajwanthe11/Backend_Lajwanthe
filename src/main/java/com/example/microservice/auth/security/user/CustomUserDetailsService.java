package com.example.microservice.auth.security.user;

import com.example.microservice.common.tenant.TenantContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base UserDetailsService with multi-tenancy support.
 * In a complete implementation, this connects to a tenant-aware UserRepository.
 * Out of the box, it provides support for default/registered users in-memory.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final Map<String, UserPrincipal> users = new ConcurrentHashMap<>();

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        // Seed default admin and user for out-of-the-box testing
        registerUser("admin", "admin@example.com", passwordEncoder.encode("admin123"), List.of("ROLE_ADMIN", "ROLE_USER"), TenantContext.DEFAULT_TENANT_ID);
        registerUser("user", "user@example.com", passwordEncoder.encode("user123"), List.of("ROLE_USER"), TenantContext.DEFAULT_TENANT_ID);
    }

    private String buildUserKey(String username, String tenantId) {
        String activeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.DEFAULT_TENANT_ID;
        return activeTenant + ":" + username;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String currentTenant = TenantContext.getTenantId();
        UserPrincipal user = users.get(buildUserKey(username, currentTenant));
        
        // Fallback check on default tenant if not found in current tenant
        if (user == null) {
            user = users.get(buildUserKey(username, TenantContext.DEFAULT_TENANT_ID));
        }

        // Direct username lookup fallback
        if (user == null) {
            user = users.get(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException(String.format("User not found with username '%s' in tenant '%s'", username, currentTenant));
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
        users.put(username, principal);
    }

    public boolean existsByUsername(String username) {
        return existsByUsernameAndTenant(username, TenantContext.getTenantId());
    }

    public boolean existsByUsernameAndTenant(String username, String tenantId) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.getTenantId();
        return users.containsKey(buildUserKey(username, effectiveTenant)) || users.containsKey(username);
    }
}

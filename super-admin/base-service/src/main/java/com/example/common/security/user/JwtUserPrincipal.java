package com.example.common.security.user;

import com.example.common.tenant.TenantContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lightweight UserPrincipal reconstructed from JWT claims.
 * Used by any microservice that validates JWT tokens but does NOT maintain a user store.
 * (i.e., every service except auth-service)
 */
public class JwtUserPrincipal implements UserDetails {

    private final String username;
    private final String tenantId;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserPrincipal(String username, String tenantId, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.tenantId = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.DEFAULT_TENANT_ID;
        this.authorities = authorities != null ? authorities : Collections.emptyList();
    }

    /**
     * Factory — creates a JwtUserPrincipal from JWT claim strings.
     *
     * @param username  Subject claim from the JWT
     * @param rolesStr  Comma-separated roles string from the JWT (e.g., "ROLE_USER,ROLE_ADMIN")
     * @param tenantId  Tenant claim from the JWT
     */
    public static JwtUserPrincipal fromClaims(String username, String rolesStr, String tenantId) {
        List<GrantedAuthority> authorities;
        if (rolesStr != null && !rolesStr.isBlank()) {
            authorities = Arrays.stream(rolesStr.split(","))
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return new JwtUserPrincipal(username, tenantId, authorities);
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return null; } // No password — trusts JWT fully
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    public String getTenantId() { return tenantId; }
}

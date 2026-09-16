package com.example.rbac.service;

import com.example.common.security.user.JwtUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserContextImpl implements CurrentUserContext {

    private JwtUserPrincipal getPrincipal() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof JwtUserPrincipal)) {
            return null;
        }

        return (JwtUserPrincipal) authentication.getPrincipal();
    }

    @Override
    public UUID getTenantId() {
        JwtUserPrincipal principal = getPrincipal();
        return principal != null ? UUID.fromString(principal.getTenantId()) : null;
    }

    @Override
    public String getUserId() {
        JwtUserPrincipal principal = getPrincipal();
        return principal != null ? principal.getUsername() : null;
    }

    @Override
    public String getUserDisplayName() {
        JwtUserPrincipal principal = getPrincipal();
        return principal != null ? principal.getUsername() : null;
    }

    @Override
    public boolean hasRole(String roleCode) {
        JwtUserPrincipal principal = getPrincipal();

        return principal != null &&
                principal.getAuthorities().stream()
                        .anyMatch(auth ->
                                auth.getAuthority().equalsIgnoreCase(roleCode)
                                || auth.getAuthority().equalsIgnoreCase("ROLE_" + roleCode));
    }
}
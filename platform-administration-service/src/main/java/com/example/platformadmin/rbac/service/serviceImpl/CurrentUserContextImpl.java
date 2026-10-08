package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.common.security.user.JwtUserPrincipal;
import com.example.platformadmin.rbac.service.CurrentUserContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CurrentUserContextImpl implements CurrentUserContext {

    private JwtUserPrincipal getPrincipal() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal;
        }

        if (principal instanceof Jwt jwt) {

            String userId = jwt.getClaimAsString("userId");
            String tenantId = jwt.getClaimAsString("tenantId");

            List < String > roles = jwt.getClaimAsStringList("roles");

            Collection < SimpleGrantedAuthority > authorities =
                    roles != null ?
                            roles.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList()) :
                            Collections.emptyList();

            return new JwtUserPrincipal(
                    userId,
                    tenantId,
                    authorities
            );
        }

        return null;
    }

    @Override
    public UUID getTenantId() {

        JwtUserPrincipal principal = getPrincipal();

        if (principal == null) {
            return null;
        }

        String tenantId = principal.getTenantId();

        if (tenantId == null || tenantId.isBlank()) {
            return null;
        }

        return UUID.fromString(tenantId);
    }

    @Override
    public String getUserId() {

        JwtUserPrincipal principal = getPrincipal();

        return principal != null ?
                principal.getUsername() :
                null;
    }

    @Override
    public String getUserDisplayName() {

        JwtUserPrincipal principal = getPrincipal();

        return principal != null ?
                principal.getUsername() :
                null;
    }

    @Override
    public boolean hasRole(String roleCode) {

        JwtUserPrincipal principal = getPrincipal();

        if (principal == null) {
            return false;
        }

        return principal.getAuthorities()
                .stream()
                .anyMatch(auth ->
                        auth.getAuthority().equalsIgnoreCase(roleCode) ||
                                auth.getAuthority()
                                        .equalsIgnoreCase("ROLE_" + roleCode));
    }
}
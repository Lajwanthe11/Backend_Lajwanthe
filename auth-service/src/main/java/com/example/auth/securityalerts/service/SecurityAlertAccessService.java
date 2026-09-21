package com.example.auth.securityalerts.service;

import com.example.auth.security.user.UserPrincipal;
import com.example.common.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Who is calling, and which tenant's security data they may touch.
 *
 * The security alert tables do not use Hibernate's automatic tenant filter (see SecurityAlert),
 * so every tenant decision for the module is made here.
 */
@Service
public class SecurityAlertAccessService {

    public static final String SYSTEM_USER = "system";

    public static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";

    private final boolean trustForwardedHeaders;

    public SecurityAlertAccessService(
            @Value("${app.security-alerts.trust-forwarded-headers:false}") boolean trustForwardedHeaders) {
        this.trustForwardedHeaders = trustForwardedHeaders;
    }

    public record Actor(String username, String tenantId, Set<String> roles) {

        public boolean isSuperAdmin() {
            return roles.contains(SUPER_ADMIN_ROLE);
        }
    }

    /** The authenticated caller, or "system" when there is none (scheduler, inter-service call). */
    public Actor currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return new Actor(SYSTEM_USER, TenantContext.getTenantId(), Set.of());
        }
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
        String tenantId = authentication.getPrincipal() instanceof UserPrincipal principal
                ? principal.getTenantId()
                : TenantContext.getTenantId();
        return new Actor(authentication.getName(), tenantId, roles);
    }

    /**
     * The tenant a list or report runs against. A Super Administrator gets the tenant they asked
     * for, or null meaning every tenant. Everyone else always gets their own tenant.
     */
    public String resolveTenantScope(String requestedTenantId) {
        Actor actor = currentActor();
        String requested = StringUtils.hasText(requestedTenantId) ? requestedTenantId.trim() : null;
        if (actor.isSuperAdmin()) {
            return requested;
        }
        if (requested != null && !requested.equals(actor.tenantId())) {
            throw new AccessDeniedException("You can only view security data of your own organization");
        }
        return actor.tenantId();
    }

    /** Callers use this to answer 404 for another tenant's record, so its existence is not revealed. */
    public boolean canAccessTenant(String tenantId) {
        Actor actor = currentActor();
        return actor.isSuperAdmin() || Objects.equals(actor.tenantId(), tenantId);
    }

    // ---------------------------------------------------------------
    // HTTP request details
    // ---------------------------------------------------------------

    public Optional<HttpServletRequest> currentRequest() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                ? Optional.of(attributes.getRequest())
                : Optional.empty();
    }

    /**
     * Client IP of a request. X-Forwarded-For is only honoured when explicitly enabled, because
     * without a trusted proxy in front any client can put an arbitrary address in it.
     */
    public String clientIp(HttpServletRequest request) {
        if (trustForwardedHeaders) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(forwarded)) {
                return forwarded.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}

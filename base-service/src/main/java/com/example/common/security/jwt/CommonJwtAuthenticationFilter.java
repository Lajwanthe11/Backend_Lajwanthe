package com.example.common.security.jwt;

import com.example.common.security.user.JwtUserPrincipal;
import com.example.common.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Shared JWT Authentication Filter — available to downstream microservices via base-service.
 *
 * Validates the JWT from the Authorization header locally using {@link JwtTokenValidator}
 * (shared secret), then reconstructs a {@link JwtUserPrincipal} from the token claims —
 * no user store lookup required.
 *
 * Used by microservices (e.g. platform-administration-service).
 * Disabled in auth-service via app.security.common-jwt-filter.enabled=false because
 * auth-service provides its own filter with user details and token revocation checks.
 */
@Component("commonJwtAuthenticationFilter")
@ConditionalOnProperty(name = "app.security.common-jwt-filter.enabled", havingValue = "true", matchIfMissing = true)
public class CommonJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CommonJwtAuthenticationFilter.class);

    private final JwtTokenValidator tokenValidator;

    public CommonJwtAuthenticationFilter(JwtTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenValidator.validateToken(jwt)) {
                String username = tokenValidator.getUsernameFromJWT(jwt);
                String rolesStr  = tokenValidator.getRolesFromJWT(jwt);
                String tenantId  = tokenValidator.getTenantIdFromJWT(jwt);

                // Sync TenantContext with the JWT tenant claim
                if (StringUtils.hasText(tenantId)) {
                    TenantContext.setTenantId(tenantId);
                }

                // Reconstruct principal from claims — no DB lookup needed
                JwtUserPrincipal principal = JwtUserPrincipal.fromClaims(username, rolesStr, tenantId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

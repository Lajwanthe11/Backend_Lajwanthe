package com.example.auth.security.jwt;

import com.example.auth.security.user.CustomUserDetailsService;
import com.example.auth.service.TokenDenylistService;
import com.example.common.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts incoming HTTP requests, extracts the JWT token from the
 * Authorization header, validates it, checks the token denylist (logout revocation),
 * sets the TenantContext, and establishes the Spring Security Context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenDenylistService tokenDenylistService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService customUserDetailsService,
                                   TokenDenylistService tokenDenylistService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.tokenDenylistService = tokenDenylistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // Reject tokens that have been explicitly revoked (e.g., via logout)
                if (tokenDenylistService.isRevoked(jwt)) {
                    log.debug("Rejected revoked JWT token");
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = tokenProvider.getUsernameFromJWT(jwt);
                String tenantId = tokenProvider.getTenantIdFromJWT(jwt);

                // Synchronize TenantContext with JWT claim
                if (StringUtils.hasText(tenantId)) {
                    TenantContext.setTenantId(tenantId);
                }

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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


package com.example.microservice.common.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that extracts the tenant identifier from HTTP request headers
 * and populates the TenantContext for the lifecycle of the request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    @Value("${app.tenant.header-name:X-Tenant-ID}")
    private String tenantHeaderName;

    @Value("${app.tenant.default-tenant:default}")
    private String defaultTenantId;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantId = request.getHeader(tenantHeaderName);

            if (StringUtils.hasText(tenantId)) {
                TenantContext.setTenantId(tenantId.trim());
                log.trace("Resolved tenant '{}' from header '{}'", tenantId, tenantHeaderName);
            } else {
                TenantContext.setTenantId(defaultTenantId);
                log.trace("No tenant header found. Using default tenant '{}'", defaultTenantId);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

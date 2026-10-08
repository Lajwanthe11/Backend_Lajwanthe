package com.example.platformadmin.rbac.aspect;

import com.example.platformadmin.rbac.config.PublicEndpoint;
import com.example.platformadmin.rbac.config.RequirePermission;
import com.example.platformadmin.rbac.dto.response.AuthenticatedUser;
import com.example.platformadmin.rbac.exception.PermissionDeniedException;
import com.example.platformadmin.rbac.service.serviceImpl.PermissionAuthorizationService;
import com.example.platformadmin.rbac.service.serviceImpl.SecurityEventLogger;
import com.example.platformadmin.rbac.util.SecurityContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionAuthorizationAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAuthorizationAspect.class);

    private final PermissionAuthorizationService authorizationService;
    private final SecurityContextUtil securityContextUtil;
    private final SecurityEventLogger securityEventLogger;

    @Value("${app.rbac.fail-closed:false}")
    private boolean failClosed;

    public PermissionAuthorizationAspect(PermissionAuthorizationService authorizationService,
            SecurityContextUtil securityContextUtil,
            SecurityEventLogger securityEventLogger) {
        this.authorizationService = authorizationService;
        this.securityContextUtil = securityContextUtil;
        this.securityEventLogger = securityEventLogger;
    }

    @Around("within(com.example.platformadmin..*) && ("
            + "within(@org.springframework.web.bind.annotation.RestController *) "
            + "|| within(@org.springframework.stereotype.Controller *))")
    public Object enforcePermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // --- Check @RequirePermission (method, then class) ---
        RequirePermission requirePermission = AnnotatedElementUtils.findMergedAnnotation(
                method, RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = AnnotatedElementUtils.findMergedAnnotation(
                    method.getDeclaringClass(), RequirePermission.class);
        }

        if (requirePermission != null) {
            // Permission check required — enforce it.
            try {
                authorizationService.authorize(requirePermission);
            } catch (PermissionDeniedException denied) {
                logDeniedEvent(denied);
                throw denied;
            }
            return joinPoint.proceed();
        }

        // --- Check @PublicEndpoint (method, then class) ---
        PublicEndpoint publicEndpoint = AnnotatedElementUtils.findMergedAnnotation(
                method, PublicEndpoint.class);
        if (publicEndpoint == null) {
            publicEndpoint = AnnotatedElementUtils.findMergedAnnotation(
                    method.getDeclaringClass(), PublicEndpoint.class);
        }

        if (publicEndpoint != null) {
            // Explicitly marked public — allow through.
            return joinPoint.proceed();
        }

        // --- UNANNOTATED ENDPOINT HANDLING ---
        String methodRef = method.getDeclaringClass().getSimpleName() + "#" + method.getName();
        if (!failClosed) {
            // Permissive mode during development/transition:
            // Allow authenticated requests through (still protected by Spring Security JWT filter).
            log.debug("[RBAC] Endpoint {} has no @RequirePermission or @PublicEndpoint — permitted (fail-closed=false).", methodRef);
            return joinPoint.proceed();
        }

        // --- DENY BY DEFAULT (Strict production mode) ---
        // Fail closed: treat as unauthorized rather than silently allowing access.
        throw new PermissionDeniedException(
                "Endpoint " + methodRef + " has no @RequirePermission or @PublicEndpoint — "
                        + "access denied by default (fail-closed policy).",
                "UNDEFINED");
    }

    private void logDeniedEvent(PermissionDeniedException denied) {
        String endpoint = "UNKNOWN";
        String ip = "UNKNOWN";
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            endpoint = request.getMethod() + " " + request.getRequestURI();
            ip = extractClientIp(request);
        }

        String userId = "UNKNOWN";
        String tenantId = "UNKNOWN";
        try {
            AuthenticatedUser user = securityContextUtil.currentUser();
            userId = user.userId();
            tenantId = user.tenantId() != null ? user.tenantId() : "UNKNOWN";
        } catch (IllegalStateException ignored) {
            // no authenticated principal available - still log what we know
        }

        securityEventLogger.logAccessDenied(
                userId, tenantId, denied.getRequestedPermission(), endpoint, ip);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

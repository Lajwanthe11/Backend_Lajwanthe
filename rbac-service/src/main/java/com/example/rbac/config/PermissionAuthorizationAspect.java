package com.example.rbac.config;

import com.example.rbac.dto.AuthenticatedUser;
import com.example.rbac.service.PermissionAuthorizationService;
import com.example.rbac.service.PermissionDeniedException;
import com.example.rbac.service.SecurityEventLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionAuthorizationAspect {

    private final PermissionAuthorizationService authorizationService;
    private final SecurityContextUtil securityContextUtil;
    private final SecurityEventLogger securityEventLogger;

    public PermissionAuthorizationAspect(PermissionAuthorizationService authorizationService,
            SecurityContextUtil securityContextUtil,
            SecurityEventLogger securityEventLogger) {
        this.authorizationService = authorizationService;
        this.securityContextUtil = securityContextUtil;
        this.securityEventLogger = securityEventLogger;
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *) "
            + "|| within(@org.springframework.stereotype.Controller *)")
    public Object enforcePermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequirePermission requirePermission = AnnotatedElementUtils.findMergedAnnotation(
                method, RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = AnnotatedElementUtils.findMergedAnnotation(
                    method.getDeclaringClass(), RequirePermission.class);
        }

        if (requirePermission == null) {
            // No annotation => treated as PUBLIC (see UnannotatedEndpointScanner
            // for the startup-time warning that catches this).
            return joinPoint.proceed();
        }

        try {
            authorizationService.authorize(requirePermission);
        } catch (PermissionDeniedException denied) {
            logDeniedEvent(denied);
            throw denied;
        }

        return joinPoint.proceed();
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

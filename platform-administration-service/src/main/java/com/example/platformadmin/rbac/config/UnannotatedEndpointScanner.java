package com.example.platformadmin.rbac.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

//  Scans controller endpoints when the application starts and reports endpoints that have neither
//@RequirePermission nor @PublicEndpoint.

@Component
public class UnannotatedEndpointScanner {

    private static final Logger log = LoggerFactory.getLogger(UnannotatedEndpointScanner.class);

    private final RequestMappingHandlerMapping handlerMapping;

    public UnannotatedEndpointScanner(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void scanOnStartup() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        int unprotected = 0;
        int publicEndpoints = 0;

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();

            boolean hasPermission = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(),
                    RequirePermission.class) != null
                    || AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(),
                            RequirePermission.class) != null;

            boolean isPublic = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(),
                    PublicEndpoint.class) != null
                    || AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(),
                            PublicEndpoint.class) != null;

            if (hasPermission) {
                // Good — permission-protected endpoint.
                continue;
            }

            if (isPublic) {
                // Explicitly opted out of permission checks.
                publicEndpoints++;
                PublicEndpoint annotation = AnnotatedElementUtils.findMergedAnnotation(
                        handlerMethod.getMethod(), PublicEndpoint.class);
                if (annotation == null) {
                    annotation = AnnotatedElementUtils.findMergedAnnotation(
                            handlerMethod.getBeanType(), PublicEndpoint.class);
                }
                log.info("[RBAC-STARTUP] Public endpoint {} on {} — reason: \"{}\"",
                        entry.getKey(), handlerMethod.getMethod().getName(),
                        annotation != null ? annotation.reason() : "N/A");
                continue;
            }

            // Neither annotation present — this endpoint will be BLOCKED at runtime
            // by PermissionAuthorizationAspect (deny-by-default policy).
            unprotected++;
            log.error("[RBAC-STARTUP-ERROR] Endpoint {} on {}.{} has NEITHER @RequirePermission NOR "
                    + "@PublicEndpoint. It will be BLOCKED at runtime (fail-closed policy). "
                    + "Add one of these annotations to fix.",
                    entry.getKey(),
                    handlerMethod.getBeanType().getSimpleName(),
                    handlerMethod.getMethod().getName());
        }

        if (unprotected > 0) {
            log.error("[RBAC-STARTUP-ERROR] {} endpoint(s) are UNPROTECTED and will be denied at runtime. "
                    + "See errors above.", unprotected);
        } else {
            log.info("[RBAC-STARTUP] All {} controller endpoint(s) are annotated "
                    + "({} with @RequirePermission, {} with @PublicEndpoint).",
                    handlerMethods.size() - publicEndpoints + publicEndpoints,
                    handlerMethods.size() - publicEndpoints,
                    publicEndpoints);
        }
    }
}

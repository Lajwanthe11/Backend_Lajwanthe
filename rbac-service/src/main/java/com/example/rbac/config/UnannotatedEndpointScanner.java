package com.example.rbac.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

@Component
public class UnannotatedEndpointScanner {

    private static final Logger log = LoggerFactory.getLogger(UnannotatedEndpointScanner.class);

    private final RequestMappingHandlerMapping handlerMapping;

    public UnannotatedEndpointScanner(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void scanOnStartup() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        int uncovered = 0;
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();

            RequirePermission methodLevel = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getMethod(), RequirePermission.class);
            RequirePermission classLevel = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), RequirePermission.class);

            if (methodLevel == null && classLevel == null) {
                uncovered++;
                log.warn("[RBAC-STARTUP-WARNING] Endpoint {} on {} has no @RequirePermission "
                        + "- it is PUBLIC. If this is intentional, ignore this warning.",
                        entry.getKey(), handlerMethod.getMethod().getName());
            }
        }

        if (uncovered > 0) {
            log.warn("[RBAC-STARTUP-WARNING] {} endpoint(s) are unprotected (no @RequirePermission).",
                    uncovered);
        } else {
            log.info("[RBAC-STARTUP] All controller endpoints carry a @RequirePermission annotation.");
        }
    }
}

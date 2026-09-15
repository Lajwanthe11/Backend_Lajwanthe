package com.example.rbac.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Explicit opt-in marker for controller methods (or entire controllers) that
 * intentionally require no RBAC permission check.
 *
 * <p>Usage rules:
 * <ul>
 *   <li>Every controller method must carry either {@link RequirePermission}
 *       <em>or</em> {@code @PublicEndpoint} — never neither.</li>
 *   <li>Prefer method-level annotation. Class-level annotation covers all
 *       methods in that controller.</li>
 *   <li>Document <em>why</em> the endpoint is public in the {@link #reason()}
 *       attribute so the decision is auditable.</li>
 * </ul>
 *
 * <p>The {@link PermissionAuthorizationAspect} enforces deny-by-default:
 * an unannotated endpoint throws {@link com.example.rbac.exception.PermissionDeniedException}
 * at runtime, and {@link UnannotatedEndpointScanner} logs a startup error.
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicEndpoint {

    /**
     * Human-readable reason why this endpoint requires no permission check.
     * Mandatory — forces the developer to justify the decision.
     */
    String reason();
}

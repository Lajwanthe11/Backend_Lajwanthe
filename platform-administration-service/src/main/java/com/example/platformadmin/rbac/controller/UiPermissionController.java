package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.config.PublicEndpoint;
import com.example.platformadmin.rbac.util.SecurityContextUtil;
import com.example.platformadmin.rbac.dto.response.AuthenticatedUser;
import com.example.platformadmin.rbac.dto.response.ModuleAccess;
import com.example.platformadmin.rbac.dto.response.UiPermissionResponse;
import com.example.platformadmin.rbac.service.serviceImpl.FeatureVisibilityMapper;
import com.example.platformadmin.rbac.service.PermissionResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

// Returns the current user’s permissions plus module/feature visibility information for the frontend..
@RestController
@RequestMapping("/api/v1/auth/me")
public class UiPermissionController {

    // Resolves the list of permissions for the logged-in user.
    private final PermissionResolver permissionResolver;
    // Reads the current authenticated user from the security context.
    private final SecurityContextUtil securityContextUtil;
    // Maps permissions to UI-friendly module visibility flags.
    private final FeatureVisibilityMapper featureVisibilityMapper;

    // Injects all required dependencies via constructor.
    public UiPermissionController(PermissionResolver permissionResolver,
            SecurityContextUtil securityContextUtil,
            FeatureVisibilityMapper featureVisibilityMapper) {
        this.permissionResolver = permissionResolver;
        this.securityContextUtil = securityContextUtil;
        this.featureVisibilityMapper = featureVisibilityMapper;
    }

    // GET /api/v1/auth/me/permissions — returns all permissions of the current user
    // as a UI response.
    // Every authenticated user may view their own permissions; JWT auth is enforced
    // upstream by Spring Security — no extra RBAC permission check is required here.
    @PublicEndpoint(reason = "Self-service endpoint: any authenticated user may read their own permissions")
    @GetMapping("/permissions")
    public UiPermissionResponse getMyPermissions() {
        AuthenticatedUser user = securityContextUtil.currentUser();
        Set<String> permissions = permissionResolver.resolvePermissions(user.userId(), user.tenantId());
        return featureVisibilityMapper.buildResponse(permissions);
    }

    // GET /api/v1/auth/me/modules — returns only the module-level access map for
    // the current user.
    @PublicEndpoint(reason = "Self-service endpoint: any authenticated user may read their own module access")
    @GetMapping("/modules")
    public Map<String, ModuleAccess> getMyModules() {
        return getMyPermissions().modules();
    }
}

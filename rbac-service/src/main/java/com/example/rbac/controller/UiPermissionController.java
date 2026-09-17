package com.example.rbac.controller;

import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.AuthenticatedUser;
import com.example.rbac.dto.ModuleAccess;
import com.example.rbac.dto.UiPermissionResponse;
import com.example.rbac.service.FeatureVisibilityMapper;
import com.example.rbac.service.PermissionResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

// Returns the current user's permissions and module access for UI rendering.
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

    // GET /api/v1/auth/me/permissions — returns all permissions of the current user as a UI response.
    @GetMapping("/permissions")
    public UiPermissionResponse getMyPermissions() {
        AuthenticatedUser user = securityContextUtil.currentUser();
        Set<String> permissions = permissionResolver.resolvePermissions(user.userId(), user.tenantId());
        return featureVisibilityMapper.buildResponse(permissions);
    }

    // GET /api/v1/auth/me/modules — returns only the module-level access map for the current user.
    @GetMapping("/modules")
    public Map<String, ModuleAccess> getMyModules() {
        return getMyPermissions().modules();
    }
}

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

@RestController
@RequestMapping("/api/v1/auth/me")
public class UiPermissionController {

    private final PermissionResolver permissionResolver;
    private final SecurityContextUtil securityContextUtil;
    private final FeatureVisibilityMapper featureVisibilityMapper;

    public UiPermissionController(PermissionResolver permissionResolver,
            SecurityContextUtil securityContextUtil,
            FeatureVisibilityMapper featureVisibilityMapper) {
        this.permissionResolver = permissionResolver;
        this.securityContextUtil = securityContextUtil;
        this.featureVisibilityMapper = featureVisibilityMapper;
    }

    @GetMapping("/permissions")
    public UiPermissionResponse getMyPermissions() {
        AuthenticatedUser user = securityContextUtil.currentUser();
        Set<String> permissions = permissionResolver.resolvePermissions(user.userId(), user.tenantId());
        return featureVisibilityMapper.buildResponse(permissions);
    }

    @GetMapping("/modules")
    public Map<String, ModuleAccess> getMyModules() {
        return getMyPermissions().modules();
    }
}

package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.entity.Permission;
import com.example.platformadmin.rbac.config.PublicEndpoint;

import com.example.platformadmin.rbac.dto.response.PermissionGroupResponseDto;
import com.example.platformadmin.rbac.dto.response.PermissionResponseDto;
import com.example.platformadmin.rbac.service.PermissionGroupService;
import com.example.platformadmin.rbac.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PublicEndpoint(reason = "Permission registry endpoint")
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionGroupService permissionGroupService;

    public PermissionController(PermissionService permissionService,
                                 PermissionGroupService permissionGroupService) {
        this.permissionService = permissionService;
        this.permissionGroupService = permissionGroupService;
    }

    // GET /api/v1/permissions?groupId=&module=&activeOnly=
    @GetMapping
    public Page<PermissionResponseDto> listPermissions(
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        return permissionService.listPermissions(groupId, module, activeOnly, pageable);
    }

    // GET /api/v1/permissions/{permId}
    @GetMapping("/{permId}")
    public PermissionResponseDto getPermission(@PathVariable UUID permId) {
        return permissionService.getById(permId);
    }
    // GET /api/v1/permissions/groups
    @GetMapping("/groups")
    public List<PermissionGroupResponseDto> listGroups() {
        return permissionGroupService.listGroups();
    }

    // GET /api/v1/permissions/groups/{groupId}
    @GetMapping("/groups/{groupId}") 
    public PermissionGroupResponseDto getGroup(@PathVariable UUID groupId) {
        return permissionGroupService.getGroupWithPermissions(groupId);
    }

    // GET /api/v1/permissions/search?query=
    @GetMapping("/search")
    public List<PermissionResponseDto> search(@RequestParam String query) {
        return permissionService.search(query);
    }

    // GET /api/v1/permissions/by-module/{mod}
    @GetMapping("/by-module/{mod}")
    public List<PermissionResponseDto> getByModule(@PathVariable("mod") String module) {
        return permissionService.getByModule(module);
    }
}

package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.config.RequirePermission;
import com.example.platformadmin.rbac.dto.response.CustomRoleResponse;
import com.example.platformadmin.rbac.dto.request.CustomRoleRequest;
import com.example.platformadmin.rbac.service.CustomRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles/custom")
public class CustomRoleController {

    private final CustomRoleService customRoleService;

    public CustomRoleController(CustomRoleService customRoleService) {
        this.customRoleService = customRoleService;
    }

    @PostMapping
    @RequirePermission("CUSTOM_ROLE_CREATE")
    public ResponseEntity<CustomRoleResponse> create(
            @Valid @RequestBody CustomRoleRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customRoleService.create(request));
    }

    @GetMapping
    @RequirePermission("CUSTOM_ROLE_READ")
    public ResponseEntity<List<CustomRoleResponse>> getAll() {

        return ResponseEntity.ok(customRoleService.getAll());
    }

    @GetMapping("/limits")
    @RequirePermission("CUSTOM_ROLE_LIMITS")
    public ResponseEntity<Object> limits() {

        return ResponseEntity.ok(customRoleService.getLimits());
    }

    @PutMapping("/{roleId}")
    @RequirePermission("CUSTOM_ROLE_UPDATE")
    public ResponseEntity<CustomRoleResponse> update(
            @PathVariable UUID roleId,
            @Valid @RequestBody CustomRoleRequest request) {

        return ResponseEntity.ok(
                customRoleService.update(roleId, request)
        );
    }

    @PostMapping("/{roleId}/publish")
    @RequirePermission("CUSTOM_ROLE_PUBLISH")
    public ResponseEntity<CustomRoleResponse> publish(
            @PathVariable UUID roleId,
            @RequestParam(required = false) String publishNotes) {

        return ResponseEntity.ok(
                customRoleService.publish(roleId, publishNotes)
        );
    }

    @PostMapping("/{roleId}/archive")
    @RequirePermission("CUSTOM_ROLE_ARCHIVE")
    public ResponseEntity<CustomRoleResponse> archive(
            @PathVariable UUID roleId) {

        return ResponseEntity.ok(
                customRoleService.archive(roleId)
        );
    }

    @GetMapping("/{roleId}/versions")
    @RequirePermission("CUSTOM_ROLE_VERSION_READ")
    public ResponseEntity<List<CustomRoleResponse>> versions(
            @PathVariable UUID roleId) {

        return ResponseEntity.ok(
                customRoleService.getVersions(roleId)
        );
    }

    @PostMapping("/{roleId}/revert/{version}")
    @RequirePermission("CUSTOM_ROLE_REVERT")
    public ResponseEntity<CustomRoleResponse> revert(
            @PathVariable UUID roleId,
            @PathVariable Integer version) {

        return ResponseEntity.ok(
                customRoleService.revert(roleId, version)
        );
    }

    @GetMapping("/{roleId}/impact")
    @RequirePermission("CUSTOM_ROLE_IMPACT")
    public ResponseEntity<Object> impact(
            @PathVariable UUID roleId) {

        return ResponseEntity.ok(
                customRoleService.getImpact(roleId)
        );
    }
}
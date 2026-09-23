package com.example.rbac.controller;

import com.example.common.abstracts.AbstractController;
import com.example.rbac.config.RequirePermission;
import com.example.rbac.dto.*;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;
import com.example.rbac.service.RoleService;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.UUID;

// REST controller for Role management
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Role management operations")
public class RoleController extends AbstractController<Role, UUID, RoleRequestDto, RoleResponseDto> {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        super(roleService);
        this.roleService = roleService;
    }

    // Search roles by name or code
    @GetMapping("/search")
    @Operation(summary = "Search and filter roles")
    public ResponseEntity<List<RoleResponseDto>> searchRoles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) RoleType roleType,
            @RequestParam(required = false) String status) {

    return ResponseEntity.ok(roleService.searchRoles(query, roleType, status));
    }

    // Activate or deactivate role
    @PatchMapping("/{id}/status")
    public ResponseEntity<RoleResponseDto> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {

    return ResponseEntity.ok(roleService.updateStatus(id, status));
    }

    // Get role count badges
    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getRoleCounts() {
    return ResponseEntity.ok(roleService.getRoleCounts());
    }

    // GET /api/v1/roles/templates — list all available role templates.
    // Only Super Admin can view templates per security responsibilities.
    @GetMapping("/templates")
    @RequirePermission("USER_READ")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<RoleTemplateSummaryDto>> listTemplates() {
        return ResponseEntity.ok(roleService.listTemplates());
    }

    // GET /api/v1/roles/templates/{id} — get template detail with permissions list
    @GetMapping("/templates/{id}")
    @RequirePermission("USER_READ")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<RoleTemplateDetailDto> getTemplateDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(roleService.getTemplateDetail(id));
    }

    // Super Admin only — hide/show a template in the library (never deletes it)
    @PatchMapping("/templates/{id}/visibility")
    @RequirePermission("USER_UPDATE")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> updateTemplateVisibility(
            @PathVariable UUID id,
            @RequestParam boolean hidden) {
        roleService.updateTemplateVisibility(id, hidden);
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/roles/{roleId}/clone — clone an existing role as a new role
    @PostMapping("/{roleId}/clone")
    @RequirePermission("USER_CREATE")
    public ResponseEntity<RoleResponseDto> cloneRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody RoleCloneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.cloneRole(roleId, request));
    }

    // GET /api/v1/roles/compare?role1Id=&role2Id= — compare permissions of two roles
    @GetMapping("/compare")
    @RequirePermission("USER_READ")
    public ResponseEntity<RoleCompareResponse> compareRoles(
            @RequestParam UUID role1Id,
            @RequestParam UUID role2Id) {
        return ResponseEntity.ok(roleService.compareRoles(role1Id, role2Id));
    }

    // GET /api/v1/roles/{roleId}/history — get audit history of a specific role
    @GetMapping("/{roleId}/history")
    @RequirePermission("USER_READ")
    public ResponseEntity<List<RoleHistoryDto>> getHistory(@PathVariable UUID roleId) {
        return ResponseEntity.ok(roleService.getHistory(roleId));
    }

    // GET /api/v1/roles/export — export role list and permissions summary
    // Endpoint produces role and permission data only — never other tenants' data.
    @GetMapping("/export")
    @RequirePermission("USER_READ")
    public ResponseEntity<byte[]> exportRoles(@RequestParam(defaultValue = "csv") String format) {
        byte[] data = roleService.exportRoles(format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=roles-export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    // GET /api/v1/roles/system — list all system (non-custom) roles
    @GetMapping("/system")
    @RequirePermission("USER_READ")
    public ResponseEntity<List<RoleResponseDto>> listSystemRoles() {
        return ResponseEntity.ok(roleService.listSystemRoles());
    }
}
package com.example.rbac.controller;

import com.example.common.abstracts.AbstractController;
import com.example.rbac.dto.*;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;
import com.example.rbac.service.serviceImpl.RoleServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

// REST controller for Role management
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Role management operations")
public class RoleController extends AbstractController<Role, Long, RoleRequestDto, RoleResponseDto> {
    private final RoleServiceImpl roleService;

    public RoleController(RoleServiceImpl roleService) {
        super(roleService);
        this.roleService = roleService;
    }

    // Search roles by name or code
    @GetMapping("/search")
    @Operation(summary = "Search and filter roles")
    public ResponseEntity<List<RoleResponseDto>> searchRoles(@RequestParam(required = false) String query,
            @RequestParam(required = false) RoleType roleType, @RequestParam(required = false) String status) {
        return ResponseEntity.ok(roleService.searchRoles(query, roleType, status));
    }

    // Activate or deactivate role
    @PatchMapping("/{id}/status")
    public ResponseEntity<RoleResponseDto> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(roleService.updateStatus(id, status));
    }

    // Get role count badges
    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getRoleCounts() {
        return ResponseEntity.ok(roleService.getRoleCounts());
    }

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping("/system")
    public List<RoleResponseDto> listSystemRoles() {
        return roleService.listSystemRoles();
    }

    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    @PostMapping("/{roleId}/clone")
    public RoleResponseDto cloneRole(@PathVariable String roleId,
                                     @Valid @RequestBody RoleCloneRequest request) {
        return roleService.cloneRole(roleId, request);
    }

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping("/compare")
    public RoleCompareResponse compareRoles(@RequestParam String role1Id,
                                            @RequestParam String role2Id) {
        return roleService.compareRoles(role1Id, role2Id);
    }

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping("/{roleId}/history")
    public List<RoleHistoryDto> getHistory(@PathVariable String roleId) {
        return roleService.getHistory(roleId);
    }

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportRoles(@RequestParam(defaultValue = "xlsx") String format) {
        byte[] file = roleService.exportRoles(format);

        MediaType mediaType = "pdf".equalsIgnoreCase(format)
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        String filename = "roles-export." + ("pdf".equalsIgnoreCase(format) ? "pdf" : "xlsx");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }
}
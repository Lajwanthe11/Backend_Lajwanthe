package com.example.rbac.controller;

import com.example.rbac.dto.RoleTemplateDetailDto;
import com.example.rbac.dto.RoleTemplateSummaryDto;
import com.example.rbac.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles/templates")
public class RoleTemplateController {

    private final RoleService roleService;

    public RoleTemplateController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Get all role templates
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @GetMapping
    public List<RoleTemplateSummaryDto> listTemplates() {
        return roleService.listTemplates();
    }

    // Get details of one role template
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @GetMapping("/{id}")
    public RoleTemplateDetailDto getTemplate(@PathVariable UUID id) {
        return roleService.getTemplateDetail(id);
    }

    // Hide or show a role template
    // Only SUPER_ADMIN can change template visibility
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{id}/hidden")
    public ResponseEntity<Void> updateVisibility(@PathVariable UUID id, @RequestParam boolean hidden) {
        roleService.updateTemplateVisibility(id, hidden);
        return ResponseEntity.noContent().build();
    }}
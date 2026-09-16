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

    // Show list of templates
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @GetMapping
    public List<RoleTemplateSummaryDto> listTemplates() {
        return roleService.listTemplates();
    }

    // Get template detials
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @GetMapping("/{id}")
    public RoleTemplateDetailDto getTemplate(@PathVariable UUID id) {
        return roleService.getTemplateDetail(id);
    }

    // Templates are never deleted, only hidden from the library — Super
    // Admin only, per the security spec.
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{id}/hidden")
    public ResponseEntity<Void> updateVisibility(@PathVariable UUID id, @RequestParam boolean hidden) {
        roleService.updateTemplateVisibility(id, hidden);
        return ResponseEntity.noContent().build();
    }}
package com.example.rbac.controller;

import com.example.rbac.dto.RoleTemplateDetailDto;
import com.example.rbac.dto.RoleTemplateSummaryDto;
import com.example.rbac.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles/templates")
public class RoleTemplateController {

    private final RoleService roleService;

    public RoleTemplateController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Super Admin manages templates platform-wide; Org Admin can view and
    // use them within their own tenant. Enforced here at the endpoint level;
    // listTemplates() also filters hidden templates by role internally.
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @GetMapping
    public List<RoleTemplateSummaryDto> listTemplates() {
        return roleService.listTemplates();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @GetMapping("/{id}")
    public RoleTemplateDetailDto getTemplate(@PathVariable String id) {
        return roleService.getTemplateDetail(id);
    }
}
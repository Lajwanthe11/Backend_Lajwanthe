package com.example.rbac.controller;

import com.example.common.abstracts.AbstractController;
import com.example.rbac.dto.RoleRequestDto;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;
import com.example.rbac.service.RoleServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

// REST controller for Role management
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Role management operations")
public class RoleController extends AbstractController< Role,Long,RoleRequestDto,RoleResponseDto> {
private final RoleServiceImpl roleService;
 public RoleController(RoleServiceImpl roleService) {
    super(roleService);
    this.roleService = roleService;
    }

// Search roles by name or code
@GetMapping("/search")
@Operation(summary = "Search and filter roles")
public ResponseEntity<List<RoleResponseDto>> searchRoles( @RequestParam(required = false) String query,@RequestParam(required = false) RoleType roleType,@RequestParam(required = false) String status) {
return ResponseEntity.ok(roleService.searchRoles(query, roleType, status));
}

// Activate or deactivate role
@PatchMapping("/{id}/status")
public ResponseEntity<RoleResponseDto> updateStatus( @PathVariable Long id,@RequestParam String status) {
return ResponseEntity.ok(roleService.updateStatus(id, status));
}

// Get role count badges
@GetMapping("/counts")
public ResponseEntity<Map<String, Long>> getRoleCounts() {
    return ResponseEntity.ok(roleService.getRoleCounts());
}
}
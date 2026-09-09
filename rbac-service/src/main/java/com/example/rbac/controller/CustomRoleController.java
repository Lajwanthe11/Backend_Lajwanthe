// CustomRoleController.java

package com.example.rbac.controller;
import com.example.rbac.dto.CustomRoleRequest;
import com.example.rbac.dto.CustomRoleResponse;
import com.example.rbac.service.CustomRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles/custom")
public class CustomRoleController {

    private final CustomRoleService customRoleService;

    public CustomRoleController(CustomRoleService customRoleService) {
        this.customRoleService = customRoleService;
    }

    @PostMapping
    public ResponseEntity<CustomRoleResponse> create(@Valid @RequestBody CustomRoleRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(customRoleService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CustomRoleResponse>> getAll() {

        return ResponseEntity.ok(customRoleService.getAll());
    }

    @GetMapping("/limits")
    public ResponseEntity<Object> limits() {

        return ResponseEntity.ok(customRoleService.getLimits());
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<CustomRoleResponse> update(@PathVariable Long roleId, @Valid @RequestBody CustomRoleRequest request) {

        return ResponseEntity.ok(customRoleService.update(roleId, request));
    }

    @PostMapping("/{roleId}/publish")
    public ResponseEntity<CustomRoleResponse> publish(@PathVariable Long roleId, @RequestParam(required = false) String publishNotes) {

        return ResponseEntity.ok(customRoleService.publish(roleId, publishNotes));
    }

    @PostMapping("/{roleId}/archive")
    public ResponseEntity<CustomRoleResponse> archive(@PathVariable Long roleId) {

        return ResponseEntity.ok(customRoleService.archive(roleId));
    }

    @GetMapping("/{roleId}/versions")
    public ResponseEntity<List<CustomRoleResponse>> versions(@PathVariable Long roleId) {

        return ResponseEntity.ok(customRoleService.getVersions(roleId));
    }

    @PostMapping("/{roleId}/revert/{version}")
    public ResponseEntity<CustomRoleResponse> revert(@PathVariable Long roleId, @PathVariable Integer version) {

        return ResponseEntity.ok(customRoleService.revert(roleId, version));
    }

    @GetMapping("/{roleId}/impact")
    public ResponseEntity<Object> impact(@PathVariable Long roleId) {

        return ResponseEntity.ok(customRoleService.getImpact(roleId));
    }
}
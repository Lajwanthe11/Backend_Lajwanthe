package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.util.SecurityContextUtil;
import com.example.platformadmin.rbac.dto.response.AuthenticatedUser;
import com.example.platformadmin.rbac.dto.response.BulkOperationResponse;
import com.example.platformadmin.rbac.dto.request.BulkRoleAssignmentRequest;
import com.example.platformadmin.rbac.dto.request.BulkRoleRevokeRequest;
import com.example.platformadmin.rbac.dto.response.CsvImportResponse;
import com.example.platformadmin.rbac.service.BulkRoleAssignmentService;
import com.example.platformadmin.rbac.service.CsvRoleImportService;


import com.example.platformadmin.rbac.config.RequirePermission;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/roles")
// Handles bulk role changes and CSV-based role imports.

public class BulkRoleAssignmentController {

    private final BulkRoleAssignmentService bulkRoleAssignmentService;
    private final CsvRoleImportService csvRoleImportService;
    private final SecurityContextUtil securityContextUtil;

    public BulkRoleAssignmentController(
            BulkRoleAssignmentService bulkRoleAssignmentService,
            CsvRoleImportService csvRoleImportService,
            SecurityContextUtil securityContextUtil
    ) {
        this.bulkRoleAssignmentService = bulkRoleAssignmentService;
        this.csvRoleImportService = csvRoleImportService;
        this.securityContextUtil = securityContextUtil;
    }
    // Assigns one role to multiple users in a single request.
    @RequirePermission("ROLE_ASSIGN")
    @PostMapping("/bulk-assign")
    public ResponseEntity<BulkOperationResponse> bulkAssign(
            @Valid @RequestBody BulkRoleAssignmentRequest request
    ) {

        AuthenticatedUser currentUser =
                securityContextUtil.currentUser();

        UUID tenantId =
                UUID.fromString(currentUser.tenantId());

        UUID actorId =
                UUID.fromString(currentUser.userId());

        return ResponseEntity.ok(
                bulkRoleAssignmentService.bulkAssign(
                        tenantId,
                        actorId,
                        request
                )
        );
    }
    // Revokes the selected role from multiple users while preserving revoke audit details.
    @RequirePermission("ROLE_ASSIGN")
    @PostMapping("/bulk-revoke")
    public ResponseEntity<BulkOperationResponse> bulkRevoke(
            @Valid @RequestBody BulkRoleRevokeRequest request
    ) {

        AuthenticatedUser currentUser =
                securityContextUtil.currentUser();

        UUID tenantId =
                UUID.fromString(currentUser.tenantId());

        UUID actorId =
                UUID.fromString(currentUser.userId());

        return ResponseEntity.ok(
                bulkRoleAssignmentService.bulkRevoke(
                        tenantId,
                        actorId,
                        request
                )
        );
    }
    // CSV import is kept separate from JSON bulk assignment because each row can succeed or fail independently.
    @RequirePermission("ROLE_ASSIGN")
    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CsvImportResponse> importAssignments(
            @RequestPart("file") MultipartFile file
    ) {

        AuthenticatedUser currentUser =
                securityContextUtil.currentUser();

        UUID tenantId =
                UUID.fromString(currentUser.tenantId());

        UUID actorId =
                UUID.fromString(currentUser.userId());

        return ResponseEntity.ok(
                csvRoleImportService.importCsv(
                        tenantId,
                        actorId,
                        file
                )
        );
    }
}
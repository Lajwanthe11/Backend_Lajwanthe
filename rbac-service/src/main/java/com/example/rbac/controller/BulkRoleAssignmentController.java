package com.example.rbac.controller;

import com.example.rbac.dto.BulkOperationResponse;
import com.example.rbac.dto.BulkRoleAssignmentRequest;
import com.example.rbac.dto.BulkRoleRevokeRequest;
import com.example.rbac.dto.CsvImportResponse;
import com.example.rbac.service.BulkRoleAssignmentService;
import com.example.rbac.service.CsvRoleImportService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/roles")
public class BulkRoleAssignmentController {

    private final BulkRoleAssignmentService bulkRoleAssignmentService;
    private final CsvRoleImportService csvRoleImportService;

    public BulkRoleAssignmentController(
            BulkRoleAssignmentService bulkRoleAssignmentService,
            CsvRoleImportService csvRoleImportService
    ) {
        this.bulkRoleAssignmentService = bulkRoleAssignmentService;
        this.csvRoleImportService = csvRoleImportService;
    }

    @PostMapping("/bulk-assign")
    public ResponseEntity<BulkOperationResponse> bulkAssign(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader("X-User-Id") UUID actorId,
            @Valid @RequestBody BulkRoleAssignmentRequest request
    ) {
        return ResponseEntity.ok(
                bulkRoleAssignmentService.bulkAssign(tenantId, actorId, request));
    }

    @PostMapping("/bulk-revoke")
    public ResponseEntity<BulkOperationResponse> bulkRevoke(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader("X-User-Id") UUID actorId,
            @Valid @RequestBody BulkRoleRevokeRequest request
    ) {
        return ResponseEntity.ok(
                bulkRoleAssignmentService.bulkRevoke(tenantId, actorId, request));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvImportResponse> importAssignments(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader("X-User-Id") UUID actorId,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(csvRoleImportService.importCsv(tenantId, actorId, file));
    }
}

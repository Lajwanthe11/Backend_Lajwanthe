package com.example.rbac.controller;

import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.*;
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
    private final SecurityContextUtil securityContextUtil;

    public BulkRoleAssignmentController(
            BulkRoleAssignmentService bulkRoleAssignmentService,
            CsvRoleImportService csvRoleImportService,SecurityContextUtil securityContextUtil
    ) {
        this.bulkRoleAssignmentService = bulkRoleAssignmentService;
        this.csvRoleImportService = csvRoleImportService;
        this.securityContextUtil = securityContextUtil;
    }

    @PostMapping("/bulk-assign")
    public ResponseEntity<BulkOperationResponse> bulkAssign(
            @Valid @RequestBody BulkRoleAssignmentRequest request
    ) {

        AuthenticatedUser currentUser = securityContextUtil.currentUser();

        UUID tenantId = UUID.fromString(currentUser.tenantId());
        UUID actorId = UUID.fromString(currentUser.userId());

        BulkOperationResponse response =
                bulkRoleAssignmentService.bulkAssign(
                        tenantId,
                        actorId,
                        request
                );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/bulk-revoke")
    public ResponseEntity<BulkOperationResponse> bulkRevoke(
            @Valid @RequestBody BulkRoleRevokeRequest request
    ) {

        AuthenticatedUser currentUser = securityContextUtil.currentUser();

        UUID tenantId = UUID.fromString(currentUser.tenantId());
        UUID actorId = UUID.fromString(currentUser.userId());

        BulkOperationResponse response =
                bulkRoleAssignmentService.bulkRevoke(
                        tenantId,
                        actorId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CsvImportResponse> importAssignments(
            @RequestPart("file") MultipartFile file
    ) {

        AuthenticatedUser currentUser = securityContextUtil.currentUser();

        UUID tenantId = UUID.fromString(currentUser.tenantId());
        UUID actorId = UUID.fromString(currentUser.userId());

        CsvImportResponse response =
                csvRoleImportService.importCsv(
                        tenantId,
                        actorId,
                        file
                );

        return ResponseEntity.ok(response);
    }
}

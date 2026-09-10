package com.enterprise.superadmin.license_management_service.controller;



import com.enterprise.superadmin.license_management_service.dto.request.LicenseAssignmentRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.service.LicenseAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/licenses")
@RequiredArgsConstructor
@Tag(
        name = "License Assignment",
        description = "License assignment APIs"
)
public class LicenseAssignmentController {

    private final LicenseAssignmentService
            assignmentService;

    @PostMapping("/{licenseId}/assign")
    @Operation(summary = "Assign license to tenant")
    public ResponseEntity<LicenseResponse> assignLicense(
            @PathVariable UUID licenseId,
            @Valid @RequestBody LicenseAssignmentRequest request
    ) {

        return ResponseEntity.ok(
                assignmentService.assignLicense(
                        licenseId,
                        request
                )
        );
    }

    @DeleteMapping("/{licenseId}/assign/{tenantId}")
    @Operation(summary = "Revoke license assignment")
    public ResponseEntity<Void> revokeLicense(
            @PathVariable UUID licenseId,
            @PathVariable UUID tenantId,
            @RequestHeader(
                    value = "X-Actor-Id",
                    required = false
            ) UUID actorId
    ) {

        assignmentService.revokeLicense(
                licenseId,
                tenantId,
                actorId
        );

        return ResponseEntity.noContent().build();
    }
}
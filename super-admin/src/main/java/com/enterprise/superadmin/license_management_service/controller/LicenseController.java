package com.enterprise.superadmin.license_management_service.controller;



import com.enterprise.superadmin.license_management_service.dto.request.LicenseCreateRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseRenewRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseUpdateRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseStatusResponse;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.service.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/licenses")
@RequiredArgsConstructor
@Tag(
        name = "License Management",
        description = "Panther License Management APIs"
)
public class LicenseController {

    private final LicenseService licenseService;

    @PostMapping
    @Operation(summary = "Create a license")
    public ResponseEntity<LicenseResponse> createLicense(
            @Valid @RequestBody LicenseCreateRequest request,
            @RequestHeader(
                    value = "X-Actor-Id",
                    required = false
            ) UUID actorId
    ) {

        LicenseResponse response =
                licenseService.createLicense(
                        request,
                        actorId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "List and filter licenses")
    public ResponseEntity<List<LicenseResponse>> getLicenses(
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) LicenseStatus status
    ) {

        return ResponseEntity.ok(
                licenseService.getAllLicenses(
                        plan,
                        status
                )
        );
    }

    @GetMapping("/{licenseId}")
    @Operation(summary = "Get license")
    public ResponseEntity<LicenseResponse> getLicense(
            @PathVariable UUID licenseId
    ) {

        return ResponseEntity.ok(
                licenseService.getLicense(licenseId)
        );
    }

    @GetMapping("/key/{licenseKey}")
    @Operation(summary = "Get license by key")
    public ResponseEntity<LicenseResponse> getByKey(
            @PathVariable String licenseKey
    ) {

        return ResponseEntity.ok(
                licenseService.getLicenseByKey(
                        licenseKey
                )
        );
    }

    @PutMapping("/{licenseId}")
    @Operation(summary = "Update license")
    public ResponseEntity<LicenseResponse> updateLicense(
            @PathVariable UUID licenseId,
            @Valid @RequestBody LicenseUpdateRequest request,
            @RequestHeader(
                    value = "X-Actor-Id",
                    required = false
            ) UUID actorId
    ) {

        return ResponseEntity.ok(
                licenseService.updateLicense(
                        licenseId,
                        request,
                        actorId
                )
        );
    }

    @PatchMapping("/{licenseId}/activate")
    @Operation(summary = "Activate license")
    public ResponseEntity<LicenseResponse> activate(
            @PathVariable UUID licenseId,
            @RequestHeader(
                    value = "X-Actor-Id",
                    required = false
            ) UUID actorId
    ) {

        return ResponseEntity.ok(
                licenseService.activateLicense(
                        licenseId,
                        actorId
                )
        );
    }

    @PatchMapping("/{licenseId}/reactivate")
    @Operation(summary = "Reactivate license")
    public ResponseEntity<LicenseResponse> reactivate(
            @PathVariable UUID licenseId,
            @RequestHeader(
                    value = "X-Actor-Id",
                    required = false
            ) UUID actorId
    ) {

        return ResponseEntity.ok(
                licenseService.reactivateLicense(
                        licenseId,
                        actorId
                )
        );
    }

    @PatchMapping("/{licenseId}/suspend")
    @Operation(summary = "Suspend license")
    public ResponseEntity<LicenseResponse> suspend(
            @PathVariable UUID licenseId,
            @RequestHeader(
                    value = "X-Actor-Id",
                    required = false
            ) UUID actorId
    ) {

        return ResponseEntity.ok(
                licenseService.suspendLicense(
                        licenseId,
                        actorId
                )
        );
    }

    @PatchMapping("/{licenseId}/renew")
    @Operation(summary = "Renew license")
    public ResponseEntity<LicenseResponse> renew(
            @PathVariable UUID licenseId,
            @Valid @RequestBody LicenseRenewRequest request,
            @RequestHeader(
                    value = "X-Actor-Id",
                    required = false
            ) UUID actorId
    ) {

        return ResponseEntity.ok(
                licenseService.renewLicense(
                        licenseId,
                        request,
                        actorId
                )
        );
    }

    @GetMapping("/{licenseId}/status")
    @Operation(summary = "Get license status")
    public ResponseEntity<LicenseStatusResponse> status(
            @PathVariable UUID licenseId
    ) {

        return ResponseEntity.ok(
                licenseService.getLicenseStatus(
                        licenseId
                )
        );
    }
}
package com.example.microservice.auth.controller;

import com.example.microservice.auth.entity.MfaPolicy;
import com.example.microservice.auth.service.MfaAuditLogService;
import com.example.microservice.auth.service.MfaPolicyService;
import com.example.microservice.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/mfa/policy")
public class MfaPolicyController {

    private final MfaPolicyService mfaPolicyService;
    private final MfaAuditLogService auditLogService;

    public MfaPolicyController(
            MfaPolicyService mfaPolicyService,
            MfaAuditLogService auditLogService) {

        this.mfaPolicyService =
                mfaPolicyService;

        this.auditLogService =
                auditLogService;
    }

    // ---------------------------------------------------------------
    // Save / Update MFA Policy
    // ---------------------------------------------------------------

    @PostMapping
    public ResponseEntity<ApiResponse<MfaPolicy>> savePolicy(
            @RequestBody MfaPolicy policy) {

        MfaPolicy savedPolicy =
                mfaPolicyService.savePolicy(policy);

        auditLogService.logEvent(
                savedPolicy.getOrganizationId(),
                savedPolicy.getOrganizationId(),
                "MFA_POLICY_SAVED",
                true,
                "MFA policy saved or updated successfully",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "MFA policy saved successfully",
                        savedPolicy
                )
        );
    }

    // ---------------------------------------------------------------
    // Get MFA Policy
    // ---------------------------------------------------------------

    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<MfaPolicy>> getPolicy(
            @PathVariable String organizationId) {

        MfaPolicy policy =
                mfaPolicyService.getPolicy(
                        organizationId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "MFA policy fetched successfully",
                        policy
                )
        );
    }

    // ---------------------------------------------------------------
    // Check Policy Exists
    // ---------------------------------------------------------------

    @GetMapping("/{organizationId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> policyExists(
            @PathVariable String organizationId) {

        boolean exists =
                mfaPolicyService.policyExists(
                        organizationId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "MFA policy existence checked successfully",
                        exists
                )
        );
    }

    // ---------------------------------------------------------------
    // Delete MFA Policy
    // ---------------------------------------------------------------

    @DeleteMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<String>> deletePolicy(
            @PathVariable String organizationId) {

        mfaPolicyService.deletePolicy(
                organizationId
        );

        auditLogService.logEvent(
                "system",
                organizationId,
                "MFA_POLICY_DELETED",
                true,
                "MFA policy deleted successfully",
                null
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "MFA policy deleted successfully",
                        "MFA_POLICY_DELETED"
                )
        );
    }
}



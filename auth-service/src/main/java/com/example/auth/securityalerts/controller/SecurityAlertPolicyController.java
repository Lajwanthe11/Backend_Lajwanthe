package com.example.auth.securityalerts.controller;

import com.example.auth.securityalerts.dto.SecurityAlertPolicyRequest;
import com.example.auth.securityalerts.dto.SecurityAlertPolicyResponse;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import com.example.auth.securityalerts.service.SecurityAlertPolicyService;
import com.example.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/security-alerts/policies")
@Tag(name = "Security Alert Policies", description = "Rules that decide which security events raise alerts")
@SecurityRequirement(name = "bearerAuth")
public class SecurityAlertPolicyController {

    /** Changing policies is for Security, Tenant and Super Administrators; other admins can only read them. */
    private static final String POLICY_MANAGERS = "hasAnyRole('SUPER_ADMIN','SECURITY_ADMIN','TENANT_ADMIN')";

    private final SecurityAlertPolicyService policyService;

    public SecurityAlertPolicyController(SecurityAlertPolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    @PreAuthorize(SecurityAlertController.ADMIN_ROLES)
    @Operation(summary = "List Alert Policies",
            description = "Platform defaults (scope GLOBAL) plus the organization's own policies (scope TENANT), which override them.")
    public ResponseEntity<ApiResponse<List<SecurityAlertPolicyResponse>>> getPolicies(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) EventType eventType) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getPolicies(tenantId, eventType)));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize(SecurityAlertController.ADMIN_ROLES)
    @Operation(summary = "View Alert Policy")
    public ResponseEntity<ApiResponse<SecurityAlertPolicyResponse>> getPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getPolicy(id)));
    }

    @PostMapping
    @PreAuthorize(POLICY_MANAGERS)
    @Operation(summary = "Create Alert Policy",
            description = "Creates the organization's policy for one event type, overriding the platform default. "
                    + "Fields left out take the platform default's value. global=true edits the platform level (Super Administrator).")
    public ResponseEntity<ApiResponse<SecurityAlertPolicyResponse>> createPolicy(
            @Valid @RequestBody SecurityAlertPolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Alert policy created", policyService.createPolicy(request)));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize(POLICY_MANAGERS)
    @Operation(summary = "Update Alert Policy", description = "Fields left out keep their current value. eventType cannot change.")
    public ResponseEntity<ApiResponse<SecurityAlertPolicyResponse>> updatePolicy(
            @PathVariable Long id, @Valid @RequestBody SecurityAlertPolicyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Alert policy updated", policyService.updatePolicy(id, request)));
    }

    @PatchMapping("/{id:\\d+}/status")
    @PreAuthorize(POLICY_MANAGERS)
    @Operation(summary = "Enable / Disable Alert Policy")
    public ResponseEntity<ApiResponse<SecurityAlertPolicyResponse>> setPolicyEnabled(
            @PathVariable Long id, @RequestParam boolean enabled) {
        return ResponseEntity.ok(ApiResponse.ok(enabled ? "Alert policy enabled" : "Alert policy disabled",
                policyService.setPolicyEnabled(id, enabled)));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize(POLICY_MANAGERS)
    @Operation(summary = "Delete Alert Policy",
            description = "Deletes an organization policy so the platform default applies again. Platform defaults can only be disabled.")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.ok("Alert policy deleted", null));
    }
}

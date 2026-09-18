package com.example.auth.securityalerts.controller;

import com.example.auth.securityalerts.dto.AlertActionRequest;
import com.example.auth.securityalerts.dto.ResolveAlertRequest;
import com.example.auth.securityalerts.dto.SecurityAlertFilter;
import com.example.auth.securityalerts.dto.SecurityAlertMetadataResponse;
import com.example.auth.securityalerts.dto.SecurityAlertResponse;
import com.example.auth.securityalerts.dto.SecurityEventResponse;
import com.example.auth.securityalerts.dto.UserSecurityAlertResponse;
import com.example.auth.securityalerts.service.SecurityAlertService;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Security Alerts screen (Story 1.6.8 / FRR-001.37).
 *
 * Declares the "bearerAuth" scheme, which gives the auth-service Swagger UI its Authorize button;
 * AuthController already referenced that name, but nothing defined it.
 */
@RestController
@RequestMapping("/security-alerts")
@Tag(name = "Security Alerts", description = "Monitor, investigate and resolve security alerts")
@SecurityRequirement(name = "bearerAuth")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SecurityAlertController {

    /** Roles that may view and work alerts. */
    static final String ADMIN_ROLES = "hasAnyRole('SUPER_ADMIN','SECURITY_ADMIN','TENANT_ADMIN','ORG_ADMIN','ADMIN')";

    private final SecurityAlertService alertService;

    public SecurityAlertController(SecurityAlertService alertService) {
        this.alertService = alertService;
    }

    // ---------------------------------------------------------------
    // Security Alerts grid
    // ---------------------------------------------------------------

    @GetMapping
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Search Alerts",
            description = "Security Alerts grid with search and the Organization, Alert Type, Severity, Status and Date Range filters. "
                    + "severity and status take several values (status=OPEN,ACKNOWLEDGED). Without sort, the most severe and "
                    + "newest alerts come first; sort=severity,desc sorts by level. Use it again for Refresh.")
    public ResponseEntity<ApiResponse<PageResponse<SecurityAlertResponse>>> searchAlerts(
            @ParameterObject SecurityAlertFilter filter,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(alertService.searchAlerts(filter, pageable))));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "View Alert Details")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> getAlert(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getAlert(id)));
    }

    @PostMapping("/{id:\\d+}/acknowledge")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Acknowledge Alert", description = "OPEN -> ACKNOWLEDGED. Body is optional.")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> acknowledgeAlert(
            @PathVariable Long id, @Valid @RequestBody(required = false) AlertActionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Alert acknowledged", alertService.acknowledgeAlert(id, request)));
    }

    @PostMapping("/{id:\\d+}/resolve")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Resolve Alert", description = "OPEN or ACKNOWLEDGED -> RESOLVED. Resolution remarks are mandatory.")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> resolveAlert(
            @PathVariable Long id, @Valid @RequestBody ResolveAlertRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Alert resolved", alertService.resolveAlert(id, request)));
    }

    @PostMapping("/{id:\\d+}/close")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Close Alert",
            description = "Any status -> CLOSED. Remarks are required unless the alert was already resolved with remarks.")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> closeAlert(
            @PathVariable Long id, @Valid @RequestBody(required = false) AlertActionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Alert closed", alertService.closeAlert(id, request)));
    }

    @GetMapping("/{id:\\d+}/events")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Alert Evidence", description = "The security events that raised or joined this alert.")
    public ResponseEntity<ApiResponse<PageResponse<SecurityEventResponse>>> getAlertEvents(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(alertService.getAlertEvents(id, pageable))));
    }

    // ---------------------------------------------------------------
    // Lookups
    // ---------------------------------------------------------------

    @GetMapping("/metadata")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Dropdown values", description = "Alert types, severities, statuses and event types.")
    public ResponseEntity<ApiResponse<SecurityAlertMetadataResponse>> getMetadata() {
        return ResponseEntity.ok(ApiResponse.ok(SecurityAlertMetadataResponse.create()));
    }

    // ---------------------------------------------------------------
    // Registered user
    // ---------------------------------------------------------------

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "My Security Alerts",
            description = "Alerts about the caller's own account, without investigation details. Any signed-in user.")
    public ResponseEntity<ApiResponse<PageResponse<UserSecurityAlertResponse>>> getMyAlerts(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(alertService.getMyAlerts(pageable))));
    }
}

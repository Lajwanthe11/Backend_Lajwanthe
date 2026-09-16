package com.example.rbac.controller;

import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.*;
import com.example.rbac.enums.RoleType;
import com.example.rbac.exception.RoleAssignmentValidationException;
import com.example.rbac.service.RoleAssignmentExportService;
import com.example.rbac.service.RoleAssignmentQueryService;
import com.example.rbac.service.RoleExpiryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles/assignments")
public class RoleAssignmentReportController {

    private final RoleAssignmentQueryService queryService;
    private final RoleAssignmentExportService exportService;
    private final RoleExpiryService expiryService;
    private final SecurityContextUtil securityContextUtil;

    public RoleAssignmentReportController(
            RoleAssignmentQueryService queryService,
            RoleAssignmentExportService exportService,
            RoleExpiryService expiryService, SecurityContextUtil securityContextUtil
    ) {
        this.queryService = queryService;
        this.exportService = exportService;
        this.expiryService = expiryService;
        this.securityContextUtil=securityContextUtil;
    }

    @GetMapping("/report")
    public ResponseEntity<?> report(
            @RequestParam(defaultValue = "json") String format
    ) {

        AuthenticatedUser currentUser = securityContextUtil.currentUser();

        UUID tenantId = UUID.fromString(currentUser.tenantId());

        List<RoleAssignmentReportRow> rows =
                queryService.getReport(tenantId);

        String normalized =
                format.toLowerCase(Locale.ROOT);

        return switch (normalized) {

            case "json" ->
                    ResponseEntity.ok(rows);

            case "xlsx", "excel" ->
                    ResponseEntity.ok()
                            .header(
                                    HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=role-assignment-report.xlsx"
                            )
                            .contentType(
                                    MediaType.parseMediaType(
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                    )
                            )
                            .body(exportService.toExcel(rows));

            case "pdf" ->
                    ResponseEntity.ok()
                            .header(
                                    HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=role-assignment-report.pdf"
                            )
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(exportService.toPdf(rows));

            default ->
                    throw new RoleAssignmentValidationException(
                            "format must be one of json, xlsx, excel, or pdf"
                    );
        };
    }

    @GetMapping("/by-department")
    public ResponseEntity<List<DepartmentRoleDistribution>> byDepartment() {

        AuthenticatedUser currentUser = securityContextUtil.currentUser();

        UUID tenantId = UUID.fromString(currentUser.tenantId());

        return ResponseEntity.ok(
                queryService.getDistributionByDepartment(tenantId)
        );
    }


    @GetMapping("/expiring")
    public ResponseEntity<List<ExpiringRoleAssignment>> expiring(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) RoleType roleType
    ) {

        AuthenticatedUser currentUser = securityContextUtil.currentUser();

        UUID tenantId = UUID.fromString(currentUser.tenantId());

        return ResponseEntity.ok(
                queryService.getExpiring(
                        tenantId,
                        days,
                        organizationId,
                        departmentId,
                        roleType
                )
        );
    }

    @PostMapping("/expiry-notify")
    public ResponseEntity<ExpiryNotificationResponse>
    triggerExpiryNotifications() {

        AuthenticatedUser currentUser = securityContextUtil.currentUser();

        UUID tenantId = UUID.fromString(currentUser.tenantId());

        return ResponseEntity.ok(
                expiryService.triggerForTenant(tenantId)
        );
    }
}

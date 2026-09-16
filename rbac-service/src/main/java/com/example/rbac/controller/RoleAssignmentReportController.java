package com.example.rbac.controller;

import com.example.rbac.dto.DepartmentRoleDistribution;
import com.example.rbac.dto.ExpiringRoleAssignment;
import com.example.rbac.dto.ExpiryNotificationResponse;
import com.example.rbac.dto.RoleAssignmentReportRow;
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

    public RoleAssignmentReportController(
            RoleAssignmentQueryService queryService,
            RoleAssignmentExportService exportService,
            RoleExpiryService expiryService
    ) {
        this.queryService = queryService;
        this.exportService = exportService;
        this.expiryService = expiryService;
    }

    @GetMapping("/report")
    public ResponseEntity<?> report(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestParam(defaultValue = "json") String format
    ) {
        List<RoleAssignmentReportRow> rows = queryService.getReport(tenantId);
        String normalized = format.toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "json" -> ResponseEntity.ok(rows);
            case "xlsx", "excel" -> ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=role-assignment-report.xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(exportService.toExcel(rows));
            case "pdf" -> ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=role-assignment-report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(exportService.toPdf(rows));
            default -> throw new RoleAssignmentValidationException(
                    "format must be one of json, xlsx, excel, or pdf");
        };
    }

    @GetMapping("/by-department")
    public ResponseEntity<List<DepartmentRoleDistribution>> byDepartment(
            @RequestHeader("X-Tenant-Id") UUID tenantId
    ) {
        return ResponseEntity.ok(queryService.getDistributionByDepartment(tenantId));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<ExpiringRoleAssignment>> expiring(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) RoleType roleType
    ) {
        return ResponseEntity.ok(queryService.getExpiring(
                tenantId, days, organizationId, departmentId, roleType));
    }

    @PostMapping("/expiry-notify")
    public ResponseEntity<ExpiryNotificationResponse> triggerExpiryNotifications(
            @RequestHeader("X-Tenant-Id") UUID tenantId
    ) {
        return ResponseEntity.ok(expiryService.triggerForTenant(tenantId));
    }
}

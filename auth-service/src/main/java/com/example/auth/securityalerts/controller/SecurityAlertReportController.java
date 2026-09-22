package com.example.auth.securityalerts.controller;

import com.example.auth.securityalerts.dto.SecurityAlertFilter;
import com.example.auth.securityalerts.dto.SecurityAlertSummaryResponse;
import com.example.auth.securityalerts.dto.SecurityAlertTrendResponse;
import com.example.auth.securityalerts.service.SecurityAlertReportService;
import com.example.auth.securityalerts.service.SecurityAlertReportService.ExportFile;
import com.example.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Reporting Engine: Alert Summary, dashboard trend and Export Report of the Security Alerts screen. */
@RestController
@RequestMapping("/security-alerts")
@Tag(name = "Security Alert Reports", description = "Alert summary, trend and CSV export")
@SecurityRequirement(name = "bearerAuth")
public class SecurityAlertReportController {

    private final SecurityAlertReportService reportService;

    public SecurityAlertReportController(SecurityAlertReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    @PreAuthorize(SecurityAlertController.ADMIN_ROLES)
    @Operation(summary = "Alert Summary",
            description = "Total, open, high severity and resolved-today counts for the summary panel, for the same filters as "
                    + "the grid except status and severity (the summary is broken down by those). Includes Last Updated / Updated By.")
    public ResponseEntity<ApiResponse<SecurityAlertSummaryResponse>> getSummary(@ParameterObject SecurityAlertFilter filter) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getSummary(filter)));
    }

    @GetMapping("/reports/trend")
    @PreAuthorize(SecurityAlertController.ADMIN_ROLES)
    @Operation(summary = "Alert Trend", description = "Alerts per day by severity for the last N days (1-90), for dashboard charts.")
    public ResponseEntity<ApiResponse<List<SecurityAlertTrendResponse>>> getTrend(
            @RequestParam(required = false) String tenantId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getTrend(tenantId, days)));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @PreAuthorize(SecurityAlertController.ADMIN_ROLES)
    @Operation(summary = "Export Report",
            description = "CSV download of the alerts matching the grid filters, most severe first. Capped at "
                    + "app.security-alerts.export-max-rows; the X-Total-Count header gives the full match count.")
    public ResponseEntity<byte[]> exportAlerts(@ParameterObject SecurityAlertFilter filter) {
        ExportFile file = reportService.exportAlerts(filter);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.fileName()).build().toString())
                .header("X-Total-Count", String.valueOf(file.matchingCount()))
                .body(file.content());
    }
}

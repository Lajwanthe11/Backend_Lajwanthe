package com.example.auth.securityalerts.service;

import com.example.auth.securityalerts.dto.SecurityAlertFilter;
import com.example.auth.securityalerts.dto.SecurityAlertSummaryResponse;
import com.example.auth.securityalerts.dto.SecurityAlertTrendResponse;

import java.util.List;

/**
 * Reporting Engine for Security Alerts: the Alert Summary panel, dashboard trend data and the
 * "Export Report" download. Every report is limited to the organizations the caller may see.
 */
public interface SecurityAlertReportService {

    /** Totals by status, severity and type for the grid's filters (status and severity filters are ignored). */
    SecurityAlertSummaryResponse getSummary(SecurityAlertFilter filter);

    /** Alerts per day by severity for the last {@code days} days (1-90), including days without alerts. */
    List<SecurityAlertTrendResponse> getTrend(String tenantId, int days);

    /** CSV of the alerts matching the grid's filters, most severe first, capped at the configured row limit. */
    ExportFile exportAlerts(SecurityAlertFilter filter);

    record ExportFile(String fileName, byte[] content, int rowCount, long matchingCount) {
    }
}

package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityAlert.AlertType;
import com.example.auth.securityalerts.entity.SecurityAlert.Severity;
import com.example.auth.securityalerts.entity.SecurityAlert.Status;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Query parameters of the alert list.
 * severity and status accept several values: status=OPEN,ACKNOWLEDGED
 */
public class SecurityAlertFilter {

    /** Matches Alert ID, username, title or IP address. */
    private String search;

    /** Organization (tenant). Only a Super Administrator can pick a tenant other than their own. */
    private String tenantId;

    private Long companyId;
    private Long departmentId;
    private AlertType alertType;
    private EventType eventType;
    private List<Severity> severity;
    private List<Status> status;
    private String username;

    /** First day included, based on alert time. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    /** Last day included, based on alert time. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public List<Severity> getSeverity() { return severity; }
    public void setSeverity(List<Severity> severity) { this.severity = severity; }
    public List<Status> getStatus() { return status; }
    public void setStatus(List<Status> status) { this.status = status; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }
    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }

    @Override
    public String toString() {
        return "search=" + search + ", tenantId=" + tenantId + ", companyId=" + companyId
                + ", departmentId=" + departmentId + ", alertType=" + alertType + ", eventType=" + eventType
                + ", severity=" + severity + ", status=" + status + ", username=" + username + ", from=" + from + ", to=" + to;
    }
}

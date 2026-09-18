package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import com.example.auth.securityalerts.entity.SecurityEvent.SourceModule;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Query parameters for the security event log. */
public class SecurityEventFilter {

    private String tenantId;
    private String username;
    private EventType eventType;
    private SourceModule sourceModule;
    private String ipAddress;

    /** true = only events that raised or joined an alert, false = only events that did not. */
    private Boolean alertRaised;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public SourceModule getSourceModule() { return sourceModule; }
    public void setSourceModule(SourceModule sourceModule) { this.sourceModule = sourceModule; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Boolean getAlertRaised() { return alertRaised; }
    public void setAlertRaised(Boolean alertRaised) { this.alertRaised = alertRaised; }
    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }
    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }
}

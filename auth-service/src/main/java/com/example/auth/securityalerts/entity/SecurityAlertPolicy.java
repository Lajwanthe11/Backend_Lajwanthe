package com.example.auth.securityalerts.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Rule that turns security events of one type into alerts.
 *
 * A policy with a null tenantId is the platform default for its event type. A tenant can
 * override it with its own policy for the same event type; the override wins, including
 * when it is disabled. There is at most one policy per (tenant, event type).
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "security_alert_policies", indexes = {
        @Index(name = "idx_security_alert_policies_lookup", columnList = "tenant_id, event_type")
})
public class SecurityAlertPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null for a platform-wide default policy. */
    @Column(name = "tenant_id", length = 100)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 60, updatable = false)
    private SecurityEvent.EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 30)
    private SecurityAlert.AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private SecurityAlert.Severity severity;

    /** Events needed inside the time window before an alert is raised. */
    @Column(name = "threshold", nullable = false)
    private int threshold;

    @Column(name = "time_window_minutes", nullable = false)
    private int timeWindowMinutes;

    /** While an open alert for the same user and event type is younger than this, new events merge into it. */
    @Column(name = "suppression_minutes", nullable = false)
    private int suppressionMinutes;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 150)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 150)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    public boolean isGlobal() {
        return tenantId == null;
    }

    // --- Getters / Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SecurityEvent.EventType getEventType() { return eventType; }
    public void setEventType(SecurityEvent.EventType eventType) { this.eventType = eventType; }
    public SecurityAlert.AlertType getAlertType() { return alertType; }
    public void setAlertType(SecurityAlert.AlertType alertType) { this.alertType = alertType; }
    public SecurityAlert.Severity getSeverity() { return severity; }
    public void setSeverity(SecurityAlert.Severity severity) { this.severity = severity; }
    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }
    public int getTimeWindowMinutes() { return timeWindowMinutes; }
    public void setTimeWindowMinutes(int timeWindowMinutes) { this.timeWindowMinutes = timeWindowMinutes; }
    public int getSuppressionMinutes() { return suppressionMinutes; }
    public void setSuppressionMinutes(int suppressionMinutes) { this.suppressionMinutes = suppressionMinutes; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public Long getVersion() { return version; }
}

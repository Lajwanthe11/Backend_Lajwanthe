package com.example.auth.securityalerts.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * A security alert raised by the alert engine from one or more {@link SecurityEvent}s.
 *
 * Does not extend BaseEntity on purpose: BaseEntity's Hibernate {@code @TenantId} limits every
 * query to the caller's tenant, but a Super Administrator must see alerts across all
 * organizations and the alert engine writes alerts for the tenant named in each event. Tenant
 * scoping is applied explicitly in SecurityAlertAccessService instead.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "security_alerts", indexes = {
        @Index(name = "idx_security_alerts_tenant_time", columnList = "tenant_id, alert_time"),
        @Index(name = "idx_security_alerts_subject", columnList = "tenant_id, event_type, subject_key, status"),
        @Index(name = "idx_security_alerts_username", columnList = "tenant_id, username")
})
public class SecurityAlert {

    public enum AlertType { AUTHENTICATION, DEVICE, ACCOUNT, POLICY, SESSION, API }

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    public enum Status {
        OPEN, ACKNOWLEDGED, RESOLVED, CLOSED;

        /** Still needs attention: can be acknowledged or resolved. */
        public boolean isActive() {
            return this == OPEN || this == ACKNOWLEDGED;
        }
    }

    public enum ResolutionStatus { PENDING, INVESTIGATING, RESOLVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable Alert ID shown on screen, e.g. SA-000042. Assigned right after insert. */
    @Column(name = "alert_code", unique = true, length = 30)
    private String alertCode;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    // --- Alert information ---

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 30)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 60)
    private SecurityEvent.EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Severity severity;

    /** Numeric copy of severity so lists can sort CRITICAL above HIGH; the enum name sorts alphabetically. */
    @Column(name = "severity_rank", nullable = false)
    private int severityRank;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_module", length = 40)
    private SecurityEvent.SourceModule sourceModule;

    @Column(name = "policy_id")
    private Long policyId;

    /** Username, or "ip:&lt;address&gt;" when the event has no user. Repeated events for the same key merge into one alert. */
    @Column(name = "subject_key", nullable = false, length = 200)
    private String subjectKey;

    @Column(name = "alert_time", nullable = false)
    private LocalDateTime alertTime;

    @Column(name = "last_occurred_at", nullable = false)
    private LocalDateTime lastOccurredAt;

    @Column(name = "occurrence_count", nullable = false)
    private int occurrenceCount;

    // --- Affected user information ---

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "username", length = 150)
    private String username;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", length = 200)
    private String departmentName;

    @Column(name = "device_id", length = 200)
    private String deviceId;

    @Column(name = "device_name", length = 200)
    private String deviceName;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "location", length = 200)
    private String location;

    // --- Alert resolution ---

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_status", nullable = false, length = 20)
    private ResolutionStatus resolutionStatus;

    @Column(name = "resolution_remarks", length = 1000)
    private String resolutionRemarks;

    @Column(name = "resolution_time")
    private LocalDateTime resolutionTime;

    @Column(name = "resolved_by", length = 150)
    private String resolvedBy;

    @Column(name = "acknowledged_by", length = 150)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "closed_by", length = 150)
    private String closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // --- Auditing ---

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Set by the service rather than @CreatedBy/@LastModifiedBy: an alert raised while a user is
    // logging in must say "system", not the name of the user in the security context.
    @Column(name = "created_by", updatable = false, length = 150)
    private String createdBy;

    @Column(name = "updated_by", length = 150)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    // --- Getters / Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAlertCode() { return alertCode; }
    public void setAlertCode(String alertCode) { this.alertCode = alertCode; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public SecurityEvent.EventType getEventType() { return eventType; }
    public void setEventType(SecurityEvent.EventType eventType) { this.eventType = eventType; }
    public Severity getSeverity() { return severity; }

    public void setSeverity(Severity severity) {
        this.severity = severity;
        this.severityRank = severity == null ? 0 : severity.ordinal() + 1;
    }

    public int getSeverityRank() { return severityRank; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SecurityEvent.SourceModule getSourceModule() { return sourceModule; }
    public void setSourceModule(SecurityEvent.SourceModule sourceModule) { this.sourceModule = sourceModule; }
    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public String getSubjectKey() { return subjectKey; }
    public void setSubjectKey(String subjectKey) { this.subjectKey = subjectKey; }
    public LocalDateTime getAlertTime() { return alertTime; }
    public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }
    public LocalDateTime getLastOccurredAt() { return lastOccurredAt; }
    public void setLastOccurredAt(LocalDateTime lastOccurredAt) { this.lastOccurredAt = lastOccurredAt; }
    public int getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public ResolutionStatus getResolutionStatus() { return resolutionStatus; }
    public void setResolutionStatus(ResolutionStatus resolutionStatus) { this.resolutionStatus = resolutionStatus; }
    public String getResolutionRemarks() { return resolutionRemarks; }
    public void setResolutionRemarks(String resolutionRemarks) { this.resolutionRemarks = resolutionRemarks; }
    public LocalDateTime getResolutionTime() { return resolutionTime; }
    public void setResolutionTime(LocalDateTime resolutionTime) { this.resolutionTime = resolutionTime; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public Long getVersion() { return version; }
}

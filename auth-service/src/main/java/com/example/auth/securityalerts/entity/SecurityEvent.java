package com.example.auth.securityalerts.entity;

import com.example.auth.securityalerts.entity.SecurityAlert.AlertType;
import com.example.auth.securityalerts.entity.SecurityAlert.Severity;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "security_events", indexes = {
        @Index(name = "idx_security_events_subject", columnList = "tenant_id, event_type, subject_key, occurred_at"),
        @Index(name = "idx_security_events_user", columnList = "tenant_id, username, occurred_at"),
        @Index(name = "idx_security_events_alert", columnList = "alert_id")
})
public class SecurityEvent {

    public enum SourceModule {
        LOGIN, LOGIN_HISTORY, MFA, PASSWORD_POLICY, ACCOUNT_LOCKOUT,
        SESSION_MANAGEMENT, USER_MANAGEMENT, API_GATEWAY,
        THREAT_DETECTION, OTHER
    }


    public enum EventType {


        LOGIN_SUCCESS(AlertType.AUTHENTICATION, Severity.LOW, 1, 15, false, "Successful login"),
        LOGIN_FAILED(AlertType.AUTHENTICATION, Severity.MEDIUM, 5, 15, true, "Multiple failed login attempts"),
        ACCOUNT_LOCKED(AlertType.ACCOUNT, Severity.HIGH, 1, 15, true, "User account locked"),
        PASSWORD_CHANGED(AlertType.ACCOUNT, Severity.LOW, 1, 15, true, "Password changed"),
        PASSWORD_POLICY_VIOLATION(AlertType.POLICY, Severity.LOW, 3, 30, true, "Repeated password policy violations"),
        MFA_FAILED(AlertType.AUTHENTICATION, Severity.HIGH, 3, 15, true, "Multiple MFA verification failures"),
        UNUSUAL_LOCATION_LOGIN(AlertType.AUTHENTICATION, Severity.HIGH, 1, 15, true, "Login from an unusual location"),
        PRIVILEGE_CHANGED(AlertType.ACCOUNT, Severity.HIGH, 1, 15, true, "Administrative privileges modified"),
        SUSPICIOUS_AUTHENTICATION(AlertType.AUTHENTICATION, Severity.CRITICAL, 1, 15, true, "Suspicious authentication behaviour"),


        SESSION_CREATED(AlertType.SESSION, Severity.LOW, 1, 15, false, "Session created"),
        CONCURRENT_SESSION_LIMIT_EXCEEDED(AlertType.SESSION, Severity.MEDIUM, 1, 15, true, "Concurrent session limit exceeded"),

        MULTIPLE_IP_SESSIONS(AlertType.SESSION, Severity.HIGH, 3, 30, true, "Sessions from multiple IP addresses"),
        SESSION_HIJACK_SUSPECTED(AlertType.SESSION, Severity.CRITICAL, 1, 15, true, "Possible session hijacking"),

        // --- Organization policy / API ---
        SECURITY_POLICY_VIOLATION(AlertType.POLICY, Severity.MEDIUM, 1, 15, true, "Organization security policy violation"),
        SUSPICIOUS_API_ACCESS(AlertType.API, Severity.HIGH, 10, 5, true, "Suspicious API access");

        private final AlertType alertType;
        private final Severity defaultSeverity;
        private final int defaultThreshold;
        private final int defaultWindowMinutes;
        private final boolean alertByDefault;
        private final String title;

        EventType(AlertType alertType, Severity defaultSeverity, int defaultThreshold, int defaultWindowMinutes,
                  boolean alertByDefault, String title) {
            this.alertType = alertType;
            this.defaultSeverity = defaultSeverity;
            this.defaultThreshold = defaultThreshold;
            this.defaultWindowMinutes = defaultWindowMinutes;
            this.alertByDefault = alertByDefault;
            this.title = title;
        }

        public AlertType getAlertType() { return alertType; }
        public Severity getDefaultSeverity() { return defaultSeverity; }
        public int getDefaultThreshold() { return defaultThreshold; }
        public int getDefaultWindowMinutes() { return defaultWindowMinutes; }
        public boolean isAlertByDefault() { return alertByDefault; }
        public String getTitle() { return title; }

        /** False when the threshold is checked by a detection rule instead of by counting events. */
        public boolean isCountThreshold() {
            return this != MULTIPLE_IP_SESSIONS;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 60)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_module", nullable = false, length = 40)
    private SourceModule sourceModule;

    /** Username, or "ip:&lt;address&gt;" when the event has no user. */
    @Column(name = "subject_key", nullable = false, length = 200)
    private String subjectKey;

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

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "device_id", length = 200)
    private String deviceId;

    @Column(name = "device_name", length = 200)
    private String deviceName;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "session_id", length = 200)
    private String sessionId;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    /** The alert this event raised or was merged into. Null when it raised no alert. */
    @Column(name = "alert_id")
    private Long alertId;

    /** Set on events produced by threat detection: the event that triggered the rule. */
    @Column(name = "derived_from_event_id")
    private Long derivedFromEventId;

    // --- Getters / Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public SourceModule getSourceModule() { return sourceModule; }
    public void setSourceModule(SourceModule sourceModule) { this.sourceModule = sourceModule; }
    public String getSubjectKey() { return subjectKey; }
    public void setSubjectKey(String subjectKey) { this.subjectKey = subjectKey; }
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
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public Long getDerivedFromEventId() { return derivedFromEventId; }
    public void setDerivedFromEventId(Long derivedFromEventId) { this.derivedFromEventId = derivedFromEventId; }
}

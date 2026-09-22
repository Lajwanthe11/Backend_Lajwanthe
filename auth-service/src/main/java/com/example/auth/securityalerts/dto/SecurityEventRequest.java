package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import com.example.auth.securityalerts.entity.SecurityEvent.SourceModule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * A security event reported by a module.
 *
 * Inside auth-service, build one and pass it to SecurityEventService.record(...):
 * <pre>
 * securityEventService.record(SecurityEventRequest.builder(EventType.LOGIN_FAILED, SourceModule.LOGIN)
 *         .username(username)
 *         .tenantId(tenantId)
 *         .details("Invalid password")
 *         .build());
 * </pre>
 * IP address and User-Agent are taken from the current HTTP request when left empty.
 * Other microservices POST the same fields as JSON to /security-alerts/events/ingest.
 */
public class SecurityEventRequest {

    @NotNull(message = "eventType is required")
    private EventType eventType;

    private SourceModule sourceModule;

    @Size(max = 100)
    private String tenantId;

    @Size(max = 100)
    private String userId;

    @Size(max = 150)
    private String username;

    private Long companyId;

    @Size(max = 200)
    private String companyName;

    private Long departmentId;

    @Size(max = 200)
    private String departmentName;

    @Size(max = 64)
    private String ipAddress;

    @Size(max = 200)
    private String deviceId;

    @Size(max = 200)
    private String deviceName;

    @Size(max = 500)
    private String userAgent;

    @Size(max = 200)
    private String location;

    @Size(max = 200)
    private String sessionId;

    @Size(max = 2000)
    private String details;

    /** When the event happened at the source. Defaults to the time it is received. */
    private LocalDateTime occurredAt;

    public SecurityEventRequest() {
    }

    public static Builder builder(EventType eventType, SourceModule sourceModule) {
        return new Builder(eventType, sourceModule);
    }

    // --- Getters / Setters ---

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public SourceModule getSourceModule() { return sourceModule; }
    public void setSourceModule(SourceModule sourceModule) { this.sourceModule = sourceModule; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
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

    public static class Builder {
        private final SecurityEventRequest request = new SecurityEventRequest();

        private Builder(EventType eventType, SourceModule sourceModule) {
            request.eventType = eventType;
            request.sourceModule = sourceModule;
        }

        public Builder tenantId(String tenantId) { request.tenantId = tenantId; return this; }
        public Builder userId(String userId) { request.userId = userId; return this; }
        public Builder username(String username) { request.username = username; return this; }
        public Builder companyId(Long companyId) { request.companyId = companyId; return this; }
        public Builder companyName(String companyName) { request.companyName = companyName; return this; }
        public Builder departmentId(Long departmentId) { request.departmentId = departmentId; return this; }
        public Builder departmentName(String departmentName) { request.departmentName = departmentName; return this; }
        public Builder ipAddress(String ipAddress) { request.ipAddress = ipAddress; return this; }
        public Builder deviceId(String deviceId) { request.deviceId = deviceId; return this; }
        public Builder deviceName(String deviceName) { request.deviceName = deviceName; return this; }
        public Builder userAgent(String userAgent) { request.userAgent = userAgent; return this; }
        public Builder location(String location) { request.location = location; return this; }
        public Builder sessionId(String sessionId) { request.sessionId = sessionId; return this; }
        public Builder details(String details) { request.details = details; return this; }
        public Builder occurredAt(LocalDateTime occurredAt) { request.occurredAt = occurredAt; return this; }

        public SecurityEventRequest build() {
            return request;
        }
    }
}

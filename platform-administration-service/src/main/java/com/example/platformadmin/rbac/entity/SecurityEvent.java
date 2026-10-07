package com.example.platformadmin.rbac.entity;

import com.example.platformadmin.rbac.enums.SecurityEventType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One row per 403 access-denied event: timestamp, userId, tenantId,
 * requestedPermission, endpoint, IP address (per the assignment's Security
 * Event Logging section). Successful permission checks are NOT logged here -
 * Part 8's scope covers denied requests only.
 */
@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant timestamp;
    private String userId;
    private String tenantId;
    private String requestedPermission;
    private String endpoint;
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private SecurityEventType eventType;

    protected SecurityEvent() {
        // JPA
    }

    public SecurityEvent(Instant timestamp, String userId, String tenantId,
                          String requestedPermission, String endpoint, String ipAddress,
                          SecurityEventType eventType) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.tenantId = tenantId;
        this.requestedPermission = requestedPermission;
        this.endpoint = endpoint;
        this.ipAddress = ipAddress;
        this.eventType = eventType;
    }

    public Long getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getRequestedPermission() {
        return requestedPermission;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public SecurityEventType getEventType() {
        return eventType;
    }
}

package com.example.devicemanagement.device.dto;

import com.example.devicemanagement.device.entity.DeviceAuditAction;

import java.time.LocalDateTime;

public class DeviceAuditLogResponse {

    private Long id;
    private Long deviceId;
    private String deviceIdentifier;
    private DeviceAuditAction action;
    private String reason;
    private String performedBy;
    private LocalDateTime performedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String deviceIdentifier) { this.deviceIdentifier = deviceIdentifier; }

    public DeviceAuditAction getAction() { return action; }
    public void setAction(DeviceAuditAction action) { this.action = action; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
}
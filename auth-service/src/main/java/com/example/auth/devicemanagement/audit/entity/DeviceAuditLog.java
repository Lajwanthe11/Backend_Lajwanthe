package com.example.auth.devicemanagement.device.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "device_audit_logs", indexes = {
        @Index(name = "idx_device_audit_logs_device_id", columnList = "device_id")
})
public class DeviceAuditLog extends BaseEntity {

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "device_identifier", nullable = false)
    private String deviceIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private DeviceAuditAction action;

    @Column(name = "reason")
    private String reason;

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String deviceIdentifier) { this.deviceIdentifier = deviceIdentifier; }

    public DeviceAuditAction getAction() { return action; }
    public void setAction(DeviceAuditAction action) { this.action = action; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
package com.example.devicemanagement.device.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices", indexes = {
        @Index(name = "idx_devices_device_identifier", columnList = "device_identifier", unique = true),
        @Index(name = "idx_devices_username", columnList = "username")
})
public class Device extends BaseEntity {

    @Column(name = "device_identifier", nullable = false, unique = true)
    private String deviceIdentifier;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "employee_id")
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    private DeviceType deviceType;

    @Column(name = "operating_system")
    private String operatingSystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "trust_status", nullable = false, length = 20)
    private TrustStatus trustStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", nullable = false, length = 20)
    private DeviceStatus deviceStatus;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String deviceIdentifier) { this.deviceIdentifier = deviceIdentifier; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public DeviceType getDeviceType() { return deviceType; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public TrustStatus getTrustStatus() { return trustStatus; }
    public void setTrustStatus(TrustStatus trustStatus) { this.trustStatus = trustStatus; }

    public DeviceStatus getDeviceStatus() { return deviceStatus; }
    public void setDeviceStatus(DeviceStatus deviceStatus) { this.deviceStatus = deviceStatus; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
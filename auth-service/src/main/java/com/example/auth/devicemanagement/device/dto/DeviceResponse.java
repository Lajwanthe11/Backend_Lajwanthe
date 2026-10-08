package com.example.auth.devicemanagement.device.dto;

import com.example.auth.devicemanagement.device.entity.DeviceStatus;
import com.example.auth.devicemanagement.device.entity.DeviceType;
import com.example.auth.devicemanagement.device.entity.TrustStatus;

import java.time.LocalDateTime;

public class DeviceResponse {

    private Long id;
    private String deviceIdentifier;
    private String deviceName;
    private String username;
    private String employeeId;
    private DeviceType deviceType;
    private String operatingSystem;
    private TrustStatus trustStatus;
    private DeviceStatus deviceStatus;
    private LocalDateTime lastLoginAt;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
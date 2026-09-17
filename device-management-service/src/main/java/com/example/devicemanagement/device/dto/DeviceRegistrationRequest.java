package com.example.devicemanagement.device.dto;

import com.example.devicemanagement.device.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;

public class DeviceRegistrationRequest {

    @NotBlank(message = "deviceIdentifier is required")
    private String deviceIdentifier;

    private String deviceName;

    private DeviceType deviceType;

    private String operatingSystem;

    private String employeeId;

    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String deviceIdentifier) { this.deviceIdentifier = deviceIdentifier; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public DeviceType getDeviceType() { return deviceType; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}
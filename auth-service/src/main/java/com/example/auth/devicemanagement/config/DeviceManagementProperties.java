package com.example.auth.devicemanagement.config;

import com.example.auth.devicemanagement.device.entity.TrustStatus;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.device-management")
public class DeviceManagementProperties {

    private TrustStatus defaultTrustStatus = TrustStatus.UNTRUSTED;
    private int maxDevicesPerUser = 5;
    private boolean auditLoggingEnabled = true;

    public TrustStatus getDefaultTrustStatus() { return defaultTrustStatus; }
    public void setDefaultTrustStatus(TrustStatus defaultTrustStatus) { this.defaultTrustStatus = defaultTrustStatus; }

    public int getMaxDevicesPerUser() { return maxDevicesPerUser; }
    public void setMaxDevicesPerUser(int maxDevicesPerUser) { this.maxDevicesPerUser = maxDevicesPerUser; }

    public boolean isAuditLoggingEnabled() { return auditLoggingEnabled; }
    public void setAuditLoggingEnabled(boolean auditLoggingEnabled) { this.auditLoggingEnabled = auditLoggingEnabled; }
}
package com.example.devicemanagement.device.config;

import com.example.devicemanagement.device.entity.TrustStatus;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.device-management")
public class DeviceManagementProperties {

    private TrustStatus defaultTrustStatus = TrustStatus.UNTRUSTED;
    private int maxDevicesPerUser = 5;
    private boolean terminateSessionsOnBlock = true;
    private boolean terminateSessionsOnRemove = true;
    private boolean auditLoggingEnabled = true;

    public TrustStatus getDefaultTrustStatus() { return defaultTrustStatus; }
    public void setDefaultTrustStatus(TrustStatus defaultTrustStatus) { this.defaultTrustStatus = defaultTrustStatus; }

    public int getMaxDevicesPerUser() { return maxDevicesPerUser; }
    public void setMaxDevicesPerUser(int maxDevicesPerUser) { this.maxDevicesPerUser = maxDevicesPerUser; }

    public boolean isTerminateSessionsOnBlock() { return terminateSessionsOnBlock; }
    public void setTerminateSessionsOnBlock(boolean terminateSessionsOnBlock) { this.terminateSessionsOnBlock = terminateSessionsOnBlock; }

    public boolean isTerminateSessionsOnRemove() { return terminateSessionsOnRemove; }
    public void setTerminateSessionsOnRemove(boolean terminateSessionsOnRemove) { this.terminateSessionsOnRemove = terminateSessionsOnRemove; }

    public boolean isAuditLoggingEnabled() { return auditLoggingEnabled; }
    public void setAuditLoggingEnabled(boolean auditLoggingEnabled) { this.auditLoggingEnabled = auditLoggingEnabled; }
}
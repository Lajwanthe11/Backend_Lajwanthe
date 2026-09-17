package com.example.devicemanagement.device.dto;

public class DeviceSummaryResponse {

    private long registeredDevices;
    private long trustedDevices;
    private long blockedDevices;
    private long inactiveDevices;

    public DeviceSummaryResponse() {
    }

    public DeviceSummaryResponse(long registeredDevices, long trustedDevices, long blockedDevices, long inactiveDevices) {
        this.registeredDevices = registeredDevices;
        this.trustedDevices = trustedDevices;
        this.blockedDevices = blockedDevices;
        this.inactiveDevices = inactiveDevices;
    }

    public long getRegisteredDevices() { return registeredDevices; }
    public void setRegisteredDevices(long registeredDevices) { this.registeredDevices = registeredDevices; }

    public long getTrustedDevices() { return trustedDevices; }
    public void setTrustedDevices(long trustedDevices) { this.trustedDevices = trustedDevices; }

    public long getBlockedDevices() { return blockedDevices; }
    public void setBlockedDevices(long blockedDevices) { this.blockedDevices = blockedDevices; }

    public long getInactiveDevices() { return inactiveDevices; }
    public void setInactiveDevices(long inactiveDevices) { this.inactiveDevices = inactiveDevices; }
}
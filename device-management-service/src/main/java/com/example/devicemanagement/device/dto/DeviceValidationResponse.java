package com.example.devicemanagement.device.dto;

/**
 * Returned to auth-service during login so it can enforce:
 * "Blocked devices are denied access until reactivated" and
 * "Trusted device information is validated during authentication."
 */
public class DeviceValidationResponse {

    private boolean registered;
    private boolean blocked;
    private boolean trusted;
    private String message;

    public DeviceValidationResponse() {
    }

    public DeviceValidationResponse(boolean registered, boolean blocked, boolean trusted, String message) {
        this.registered = registered;
        this.blocked = blocked;
        this.trusted = trusted;
        this.message = message;
    }

    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public boolean isTrusted() { return trusted; }
    public void setTrusted(boolean trusted) { this.trusted = trusted; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
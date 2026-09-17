package com.example.microservice.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class MfaVerificationRequestDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "OTP is required")
    private String otp;

    private String tenantId;

    public MfaVerificationRequestDTO() {
    }

    public MfaVerificationRequestDTO(String username, String otp, String tenantId) {
        this.username = username;
        this.otp = otp;
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}



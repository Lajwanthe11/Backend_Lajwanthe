package com.example.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mfa_policy")
public class MfaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String organizationId;

    @Column(nullable = false)
    private boolean mfaEnabled;

    @Column(nullable = false)
    private String enforcement;

    @Column(nullable = false)
    private boolean emailOtpEnabled;

    @Column(nullable = false)
    private boolean smsOtpEnabled;

    @Column(nullable = false)
    private boolean authenticatorAppEnabled;

    @Column(nullable = false)
    private boolean securityKeyEnabled;

    @Column(nullable = false)
    private int otpValidityMinutes;

    @Column(nullable = false)
    private int maxVerificationAttempts;

    @Column(nullable = false)
    private int trustedDeviceDurationDays;

    @Column(nullable = false)
    private String userScope;

    private String selectedRoles;

    private String selectedDepartments;

    @Column(nullable = false)
    private boolean recoveryCodesEnabled;

    private String backupEmail;

    private String backupMobileNumber;

    public MfaPolicy() {
    }

    public Long getId() {
        return id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public String getEnforcement() {
        return enforcement;
    }

    public void setEnforcement(String enforcement) {
        this.enforcement = enforcement;
    }

    public boolean isEmailOtpEnabled() {
        return emailOtpEnabled;
    }

    public void setEmailOtpEnabled(boolean emailOtpEnabled) {
        this.emailOtpEnabled = emailOtpEnabled;
    }

    public boolean isSmsOtpEnabled() {
        return smsOtpEnabled;
    }

    public void setSmsOtpEnabled(boolean smsOtpEnabled) {
        this.smsOtpEnabled = smsOtpEnabled;
    }

    public boolean isAuthenticatorAppEnabled() {
        return authenticatorAppEnabled;
    }

    public void setAuthenticatorAppEnabled(
            boolean authenticatorAppEnabled) {
        this.authenticatorAppEnabled =
                authenticatorAppEnabled;
    }

    public boolean isSecurityKeyEnabled() {
        return securityKeyEnabled;
    }

    public void setSecurityKeyEnabled(
            boolean securityKeyEnabled) {
        this.securityKeyEnabled =
                securityKeyEnabled;
    }

    public int getOtpValidityMinutes() {
        return otpValidityMinutes;
    }

    public void setOtpValidityMinutes(
            int otpValidityMinutes) {
        this.otpValidityMinutes =
                otpValidityMinutes;
    }

    public int getMaxVerificationAttempts() {
        return maxVerificationAttempts;
    }

    public void setMaxVerificationAttempts(
            int maxVerificationAttempts) {
        this.maxVerificationAttempts =
                maxVerificationAttempts;
    }

    public int getTrustedDeviceDurationDays() {
        return trustedDeviceDurationDays;
    }

    public void setTrustedDeviceDurationDays(
            int trustedDeviceDurationDays) {
        this.trustedDeviceDurationDays =
                trustedDeviceDurationDays;
    }

    public String getUserScope() {
        return userScope;
    }

    public void setUserScope(String userScope) {
        this.userScope = userScope;
    }

    public String getSelectedRoles() {
        return selectedRoles;
    }

    public void setSelectedRoles(String selectedRoles) {
        this.selectedRoles = selectedRoles;
    }

    public String getSelectedDepartments() {
        return selectedDepartments;
    }

    public void setSelectedDepartments(
            String selectedDepartments) {
        this.selectedDepartments =
                selectedDepartments;
    }

    public boolean isRecoveryCodesEnabled() {
        return recoveryCodesEnabled;
    }

    public void setRecoveryCodesEnabled(
            boolean recoveryCodesEnabled) {
        this.recoveryCodesEnabled =
                recoveryCodesEnabled;
    }

    public String getBackupEmail() {
        return backupEmail;
    }

    public void setBackupEmail(String backupEmail) {
        this.backupEmail = backupEmail;
    }

    public String getBackupMobileNumber() {
        return backupMobileNumber;
    }

    public void setBackupMobileNumber(
            String backupMobileNumber) {
        this.backupMobileNumber =
                backupMobileNumber;
    }
}



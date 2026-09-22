package com.example.auth.loginhistory.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA entity for one authentication attempt — successful or failed — and, for a successful one,
 * the session it opened.
 * Extends {@link BaseEntity} which provides id, audit fields (createdAt, updatedAt, etc.)
 * and multi-tenant isolation via the tenantId column.
 */
@Entity
@Table(name = "login_history",
        indexes = @Index(name = "idx_login_history_tenant_login_time", columnList = "tenant_id, login_time"))
public class LoginHistory extends BaseEntity {

    /** Outcome of a single authentication attempt. */
    public enum LoginStatus {
        SUCCESS,
        FAILED
    }

    /** How the user authenticated. OAUTH2 records also carry the provider name (google, github, ...). */
    public enum AuthenticationMethod {
        PASSWORD,
        OAUTH2,
        SSO
    }

    /**
     * How the session behind a successful login was closed.
     * MANUAL       - the user themselves called POST /auth/logout (the "evening checkout").
     * AUTO         - closed automatically because its refresh token expired without a manual logout.
     * ADMIN_FORCED - an administrator closed it via POST /login-history/{id}/logout.
     * Null while the session is still open.
     */
    public enum LogoutType {
        MANUAL,
        AUTO,
        ADMIN_FORCED
    }

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoginStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "authentication_method", nullable = false, length = 20)
    private AuthenticationMethod authenticationMethod;

    @Column(name = "auth_provider", length = 50)
    private String authProvider;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    /** How the session was closed (manual / auto / admin-forced). Null while the session is open. */
    @Enumerated(EnumType.STRING)
    @Column(name = "logout_type", length = 20)
    private LogoutType logoutType;

    /** The "sid" claim carried by the session's access and refresh tokens; null for failed attempts. */
    @Column(name = "session_id", unique = true, length = 36)
    private String sessionId;

    @Column(name = "session_expires_at")
    private LocalDateTime sessionExpiresAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "operating_system", length = 50)
    private String operatingSystem;

    @Column(name = "browser", length = 50)
    private String browser;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LoginStatus getStatus() { return status; }
    public void setStatus(LoginStatus status) { this.status = status; }

    public AuthenticationMethod getAuthenticationMethod() { return authenticationMethod; }
    public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) { this.authenticationMethod = authenticationMethod; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }

    public LogoutType getLogoutType() { return logoutType; }
    public void setLogoutType(LogoutType logoutType) { this.logoutType = logoutType; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getSessionExpiresAt() { return sessionExpiresAt; }
    public void setSessionExpiresAt(LocalDateTime sessionExpiresAt) { this.sessionExpiresAt = sessionExpiresAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
}

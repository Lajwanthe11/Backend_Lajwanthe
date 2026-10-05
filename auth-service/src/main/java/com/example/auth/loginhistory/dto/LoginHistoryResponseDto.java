package com.example.auth.loginhistory.dto;

import com.example.auth.loginhistory.entity.LoginHistory.AuthenticationMethod;
import com.example.auth.loginhistory.entity.LoginHistory.LoginStatus;
import com.example.auth.loginhistory.entity.LoginHistory.LogoutType;

import java.time.LocalDateTime;

/**
 * Response DTO returned for Login History API responses.
 * tenantId is the tenant the record belongs to (FK to tenants.tenant_id).
 * sessionStatus is null for failed attempts; sessionDurationSeconds is null until logout.
 * logoutType is null while the session is still open.
 * workHoursStatus is null for failed attempts; it compares the time worked so far against the
 * required work-day length (app.login-history.required-work-minutes, 9h by default).
 */
public record LoginHistoryResponseDto(
        Long id,
        String tenantId,
        String username,
        String email,
        LoginStatus status,
        AuthenticationMethod authenticationMethod,
        String authProvider,
        String failureReason,
        LocalDateTime loginTime,
        LocalDateTime logoutTime,
        SessionStatus sessionStatus,
        LogoutType logoutType,
        Long sessionDurationSeconds,
        WorkHoursStatus workHoursStatus,
        Long requiredWorkSeconds,
        String ipAddress,
        String location,
        String deviceType,
        String operatingSystem,
        String browser,
        String userAgent
) {

    /** State of the session opened by a successful login. Derived when the record is read, not stored. */
    public enum SessionStatus {
        ACTIVE,
        LOGGED_OUT,
        EXPIRED
    }

    /**
     * How the time worked so far compares against the required work-day length.
     * IN_PROGRESS - session still open, required hours not yet reached.
     * COMPLETED   - session still open, required hours reached (or exceeded, not yet by much).
     * SHORTFALL   - session closed before the required hours were worked.
     * OVERTIME    - session closed after working past the required hours.
     */
    public enum WorkHoursStatus {
        IN_PROGRESS,
        COMPLETED,
        SHORTFALL,
        OVERTIME
    }
}

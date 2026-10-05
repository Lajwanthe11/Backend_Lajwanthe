package com.example.auth.loginhistory.service;

import com.example.auth.loginhistory.dto.LoginHistoryResponseDto;
import com.example.auth.loginhistory.dto.LoginHistorySearchCriteria;
import com.example.auth.loginhistory.dto.LoginHistorySummaryDto;
import com.example.auth.loginhistory.dto.LoginHoursStatusDto;
import com.example.auth.loginhistory.entity.LoginHistory.AuthenticationMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.AuthenticationException;

import java.util.Date;

/**
 * Service contract for Login History: querying authentication records, and recording them from
 * the authentication flow. Recording is best-effort — a failure to write history is logged and
 * never fails the login, logout or refresh that triggered it.
 */
public interface LoginHistoryService {

    Page<LoginHistoryResponseDto> search(LoginHistorySearchCriteria criteria, Pageable pageable);

    LoginHistoryResponseDto getById(Long id);

    Page<LoginHistoryResponseDto> getHistoryForUser(String username, Pageable pageable);

    LoginHistorySummaryDto getSummary();

    /**
     * The caller's login-hours status: check-in time, checkout time (if any), hours worked so
     * far, hours remaining against the required work day, and whether logout was manual, automatic
     * or admin-forced. Based on the caller's most recent login record.
     */
    LoginHoursStatusDto getMyLoginStatus(String username);

    /** CSV of every record matching the criteria, newest first. */
    byte[] exportCsv(LoginHistorySearchCriteria criteria);

    void recordSuccessfulLogin(String username, String email, AuthenticationMethod method,
                               String provider, String sessionId, Date sessionExpiresAt);

    void recordFailedLogin(String username, AuthenticationMethod method, String provider,
                           AuthenticationException cause);

    /**
     * Closes the session's record as a user-initiated ("evening checkout") logout.
     * No-op for unknown sessions or ones already logged out.
     */
    void recordLogout(String sessionId);

    /**
     * Sweeps every open session whose refresh token has already expired and closes them with
     * {@code LogoutType.AUTO} — the "it will automatically logout" behaviour. Returns the number
     * of sessions closed.
     */
    int autoCloseExpiredSessions();

    /** Moves the session's expiry forward after its refresh token was exchanged for a new pair. */
    void recordTokenRefresh(String sessionId, Date sessionExpiresAt);

    /**
     * Admin-initiated forced logout of the session tied to the given record. Closes the record's
     * session immediately, the same way {@link #recordLogout(String)} would when the user logs
     * out themselves. Fails if the record has no active session (already logged out, expired, or
     * a failed attempt, which never opens a session).
     */
    LoginHistoryResponseDto terminateSession(Long id);
}

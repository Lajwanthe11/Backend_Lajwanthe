package com.example.auth.loginhistory.dto;

import com.example.auth.loginhistory.dto.LoginHistoryResponseDto.SessionStatus;
import com.example.auth.loginhistory.dto.LoginHistoryResponseDto.WorkHoursStatus;
import com.example.auth.loginhistory.entity.LoginHistory.LogoutType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * "Login hours" status for the caller's most recent session — the check-in/checkout style widget
 * (morning check-in, evening checkout around a 9-hour work day, like a Razorpay-style attendance card).
 *
 * hasSession is false when the caller has never logged in; every other field is then null/zero.
 */
public record LoginHoursStatusDto(
        boolean hasSession,
        Long loginHistoryId,
        String username,
        LocalDate date,
        LocalDateTime loginTime,
        LocalDateTime logoutTime,
        SessionStatus sessionStatus,
        LogoutType logoutType,
        long workedSeconds,
        long requiredSeconds,
        long remainingSeconds,
        WorkHoursStatus workHoursStatus,
        double percentComplete
) {

    public static LoginHoursStatusDto none(String username, long requiredSeconds) {
        return new LoginHoursStatusDto(false, null, username, null, null, null, null, null,
                0L, requiredSeconds, requiredSeconds, null, 0.0);
    }
}

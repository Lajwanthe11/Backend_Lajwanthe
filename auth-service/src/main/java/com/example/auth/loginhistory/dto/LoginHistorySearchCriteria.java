package com.example.auth.loginhistory.dto;

import com.example.auth.loginhistory.entity.LoginHistory.AuthenticationMethod;
import com.example.auth.loginhistory.entity.LoginHistory.LoginStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Filters for the Login History screen, bound from query parameters. Every field is optional.
 *
 * @param search matched case-insensitively against username, email and employee ID
 * @param from   first day to include (yyyy-MM-dd)
 * @param to     last day to include (yyyy-MM-dd)
 */
public record LoginHistorySearchCriteria(
        String search,
        LoginStatus status,
        AuthenticationMethod authenticationMethod,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
) {
}

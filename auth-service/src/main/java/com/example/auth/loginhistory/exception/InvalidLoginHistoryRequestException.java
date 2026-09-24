package com.example.auth.loginhistory.exception;

/**
 * Thrown when the caller's request is well-formed JSON/query params but invalid at the
 * business level — a bad date range, an over-long search term, an export that exceeds the
 * row limit, or a force-logout requested on a record with no active session.
 * Mirrors {@code com.example.platformadmin.user.exception.UserAlreadyExistsException} —
 * mapped to 400 Bad Request by {@link LoginHistoryGlobalExceptionHandler}.
 */
public class InvalidLoginHistoryRequestException extends RuntimeException {

    public InvalidLoginHistoryRequestException(String message) {
        super(message);
    }
}

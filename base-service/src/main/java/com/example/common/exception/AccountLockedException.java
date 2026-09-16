package com.example.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user account is locked due to too many
 * consecutive failed login attempts.
 */
public class AccountLockedException extends AppException {

    public AccountLockedException(String message) {
        super(message, HttpStatus.LOCKED);
    }
}
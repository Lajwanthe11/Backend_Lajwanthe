package com.example.common.exception;

public class AccountLockedException extends AppException {
    public AccountLockedException(String message) {
        super(message);
    }
}
package com.example.auth.loginhistory.exception;

/**
 * Thrown when a login history record cannot be found by its id.
 * Mirrors {@code com.example.platformadmin.user.exception.UserNotFoundException} —
 * mapped to 404 Not Found by {@link LoginHistoryGlobalExceptionHandler}.
 */
public class LoginHistoryNotFoundException extends RuntimeException {

    public LoginHistoryNotFoundException(String message) {
        super(message);
    }

    public static LoginHistoryNotFoundException forId(Long id) {
        return new LoginHistoryNotFoundException("Login history not found with id: '" + id + "'");
    }
}

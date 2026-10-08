package com.example.auth.loginhistory.exception;

import com.example.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for the Login History module.
 * Mirrors {@code com.example.platformadmin.user.exception.UserGlobalExceptionHandler}: each
 * module owns its own exception types and its own advice, instead of every failure funnelling
 * through the shared, application-wide handler.
 * <p>
 * {@code @Order(HIGHEST_PRECEDENCE)} makes sure this advice is checked before the shared,
 * unscoped {@code com.example.common.exception.GlobalExceptionHandler} — otherwise its
 * catch-all {@code Exception.class} handler could shadow these more specific ones.
 */
@RestControllerAdvice(basePackages = "com.example.auth.loginhistory")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoginHistoryGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginHistoryGlobalExceptionHandler.class);

    @ExceptionHandler(LoginHistoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(LoginHistoryNotFoundException exception) {
        log.warn("Login history not found: {}", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(InvalidLoginHistoryRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(InvalidLoginHistoryRequestException exception) {
        log.warn("Invalid login history request: {}", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(LoginHistoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInfrastructureFailure(LoginHistoryException exception) {
        log.error("Login history operation failed", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(exception.getMessage()));
    }
}

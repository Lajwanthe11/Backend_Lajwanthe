package com.example.rbac.controller;

import com.example.rbac.service.PermissionGroupNotFoundException;
import com.example.rbac.service.PermissionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Scoped to the permission registry package for now. If the team already has a
 * shared @ControllerAdvice for rbac-service, merge these handlers into it instead
 * of running two advices side by side.
 */
@RestControllerAdvice(basePackages = "com.example.rbac.controller")
public class PermissionExceptionHandler {

    @ExceptionHandler({PermissionNotFoundException.class, PermissionGroupNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Not Found",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}

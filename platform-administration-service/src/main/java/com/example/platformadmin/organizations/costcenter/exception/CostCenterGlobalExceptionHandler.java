package com.example.platformadmin.organizations.costcenter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(
        basePackages = "com.example.platformadmin.organizations.costcenter"
)
public class CostCenterGlobalExceptionHandler {

    @ExceptionHandler({
            InvalidOrganizationIdException.class,
            InvalidCompanyIdException.class,
            InvalidDepartmentIdException.class
    })
    public ResponseEntity<Map<String, Object>> handleInvalidIds(
            RuntimeException ex) {

        Map<String, Object> response = new HashMap<>();

        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("status", 400);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(CostCenterNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCostCenterNotFound(
            CostCenterNotFoundException ex) {

        Map<String, Object> response = new HashMap<>();

        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("status", 404);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}
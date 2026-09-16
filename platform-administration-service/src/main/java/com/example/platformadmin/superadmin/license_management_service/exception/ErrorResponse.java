package com.example.platformadmin.superadmin.license_management_service.exception;


import java.time.LocalDateTime;

public record ErrorResponse(

        LocalDateTime timestamp,

        int status,

        String error,

        String code,

        String message
) {
}
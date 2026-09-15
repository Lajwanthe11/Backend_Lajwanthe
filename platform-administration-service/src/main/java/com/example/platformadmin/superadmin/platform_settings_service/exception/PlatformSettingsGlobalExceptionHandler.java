package com.example.platformadmin.superadmin.platform_settings_service.exception;

import com.example.platformadmin.superadmin.platform_settings_service.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class PlatformSettingsGlobalExceptionHandler {

    @ExceptionHandler(SettingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            SettingNotFoundException exception) {

        log.warn("Platform setting not found: {}", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(404)
                        .error("NOT_FOUND")
                        .message(exception.getMessage())
                        .build());
    }

    @ExceptionHandler(InvalidSettingException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSetting(
            InvalidSettingException exception) {

        log.warn("Invalid platform setting: {}", exception.getMessage());

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(400)
                        .error("INVALID_SETTING")
                        .message(exception.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {

        log.warn("Validation error occurred: {}", exception.getMessage());

        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (first, second) -> first
                ));

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(400)
                        .error("VALIDATION_ERROR")
                        .message("Invalid platform settings")
                        .validationErrors(errors)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception exception) {

        log.error(
                "An unexpected error occurred: {}",
                exception.getMessage(),
                exception
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(500)
                        .error("INTERNAL_SERVER_ERROR")
                        .message("An unexpected error occurred. Please try again later.")
                        .build());
    }
}
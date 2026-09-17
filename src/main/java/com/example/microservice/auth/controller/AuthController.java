package com.example.microservice.auth.controller;

import com.example.microservice.auth.dto.AuthResponse;
import com.example.microservice.auth.dto.LoginRequest;
import com.example.microservice.auth.dto.RegisterRequest;
import com.example.microservice.auth.dto.TokenRefreshRequest;
import com.example.microservice.auth.service.AuthService;
import com.example.microservice.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and JWT token refresh")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password", description = "Returns JWT Access Token and Refresh Token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers user credentials and returns tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("User registered successfully", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT Access Token", description = "Accepts a valid refresh token and issues a new pair")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        AuthResponse response = authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    @GetMapping("/oauth2/success")
    @Operation(summary = "OAuth2 Callback Landing Endpoint", description = "Displays successful OAuth2 login details")
    public ResponseEntity<ApiResponse<String>> oauth2Success(
            @RequestParam("token") String token,
            @RequestParam("refreshToken") String refreshToken,
            @RequestParam(value = "tenantId", required = false) String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok("OAuth2 Login Successful. Access token issued.", token));
    }
}

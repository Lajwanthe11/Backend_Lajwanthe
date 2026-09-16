package com.example.auth.controller;

import com.example.auth.dto.AuthResponseDTO;
import com.example.auth.dto.LoginRequestDTO;
import com.example.auth.dto.PasswordResetConfirmDTO;
import com.example.auth.dto.PasswordResetRequestDTO;
import com.example.auth.dto.PasswordResetResponseDTO;
import com.example.auth.dto.RegisterRequestDTO;
import com.example.auth.dto.TokenRefreshRequestDTO;
import com.example.auth.security.jwt.JwtTokenProvider;
import com.example.auth.service.AuthService;
import com.example.auth.service.PasswordResetService;
import com.example.auth.service.TokenDenylistService;
import com.example.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * REST controller exposing core authentication endpoints:
 * <ul>
 *   <li>POST /auth/login             — authenticate and receive JWT tokens</li>
 *   <li>POST /auth/register          — create a new account and receive JWT tokens</li>
 *   <li>POST /auth/logout            — revoke the current access token</li>
 *   <li>POST /auth/refresh           — exchange a refresh token for a new token pair</li>
 *   <li>POST /auth/password-reset/request  — request a password-reset token</li>
 *   <li>POST /auth/password-reset/confirm  — confirm reset with token + new password</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for registration, login, logout, token refresh, and password reset")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final TokenDenylistService tokenDenylistService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthService authService,
                          PasswordResetService passwordResetService,
                          TokenDenylistService tokenDenylistService,
                          JwtTokenProvider tokenProvider) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.tokenDenylistService = tokenDenylistService;
        this.tokenProvider = tokenProvider;
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate with username and password. Returns a JWT Access Token and Refresh Token."
    )
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {

        AuthResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    // ---------------------------------------------------------------
    // Register
    // ---------------------------------------------------------------

    @PostMapping("/register")
    @Operation(
            summary = "Register",
            description = "Register a new user account. Returns a JWT token pair on success."
    )
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO registerRequest) {

        AuthResponseDTO response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("User registered successfully", response));
    }

    // ---------------------------------------------------------------
    // Logout
    // ---------------------------------------------------------------

    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = "Revoke the caller's current JWT access token. " +
                          "The token must be passed in the Authorization header as 'Bearer <token>'. " +
                          "Once revoked, the token is rejected on all subsequent requests.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No Bearer token found in Authorization header"));
        }

        String jwt = bearerToken.substring(7);

        if (!tokenProvider.validateToken(jwt)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token is invalid or already expired"));
        }

        Date expiry = tokenProvider.getExpiryFromJWT(jwt);
        tokenDenylistService.revokeToken(jwt, expiry);

        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    // ---------------------------------------------------------------
    // Token Refresh
    // ---------------------------------------------------------------

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh JWT Access Token",
            description = "Exchange a valid refresh token for a new access token + refresh token pair."
    )
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken(
            @Valid @RequestBody TokenRefreshRequestDTO refreshRequest) {

        AuthResponseDTO response = authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    // ---------------------------------------------------------------
    // Password Reset — Step 1: Request
    // ---------------------------------------------------------------

    @PostMapping("/password-reset/request")
    @Operation(
            summary = "Request Password Reset",
            description = "Generate a time-limited password reset token for the given username. " +
                          "In production this token would be sent by email; " +
                          "it is returned directly in the response for developer convenience."
    )
    public ResponseEntity<ApiResponse<PasswordResetResponseDTO>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO resetRequest) {

        PasswordResetResponseDTO response = passwordResetService.generateResetToken(resetRequest);
        return ResponseEntity.ok(ApiResponse.ok("Password reset token generated", response));
    }

    // ---------------------------------------------------------------
    // Password Reset — Step 2: Confirm
    // ---------------------------------------------------------------

    @PostMapping("/password-reset/confirm")
    @Operation(
            summary = "Confirm Password Reset",
            description = "Validate the reset token received from /password-reset/request " +
                          "and set a new password. The token is single-use and expires in 15 minutes."
    )
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmDTO confirmRequest) {

        passwordResetService.resetPassword(confirmRequest);
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully. Please log in with your new password.", null));
    }

    // ---------------------------------------------------------------
    // OAuth2 Success Landing (unchanged)
    // ---------------------------------------------------------------

    @GetMapping("/oauth2/success")
    @Operation(summary = "OAuth2 Callback Landing Endpoint", description = "Displays successful OAuth2 login details")
    public ResponseEntity<ApiResponse<String>> oauth2Success(
            @RequestParam("token") String token,
            @RequestParam("refreshToken") String refreshToken,
            @RequestParam(value = "tenantId", required = false) String tenantId) {

        return ResponseEntity.ok(ApiResponse.ok("OAuth2 Login Successful. Access token issued.", token));
    }
}

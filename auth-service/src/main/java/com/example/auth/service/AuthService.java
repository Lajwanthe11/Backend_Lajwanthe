package com.example.auth.service;

import com.example.auth.dto.AuthResponseDTO;
import com.example.auth.dto.LoginRequestDTO;
import com.example.auth.dto.RegisterRequestDTO;
import com.example.auth.dto.TokenRefreshRequestDTO;
import com.example.auth.security.jwt.JwtTokenProvider;
import com.example.auth.security.user.CustomUserDetailsService;
import com.example.common.exception.AccountLockedException;
import com.example.common.exception.BadRequestException;
import com.example.common.tenant.TenantContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core authentication business logic: login, registration, and token refresh.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    // NEW: password strength validation added for registration flow
    private final PasswordValidator passwordValidator;
    private final SessionManagementService sessionManagementService;

    public AuthService(AuthenticationManager authenticationManager,
                       CustomUserDetailsService customUserDetailsService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       PasswordValidator passwordValidator,SessionManagementService sessionManagementService) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.passwordValidator = passwordValidator;
        this.sessionManagementService = sessionManagementService;
    }

    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        if (StringUtils.hasText(loginRequest.getTenantId())) {
            TenantContext.setTenantId(loginRequest.getTenantId());
        }
        String tenantId = TenantContext.getTenantId();
        // Declared here (not inside try) since it's only assigned on success but needed below
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (LockedException ex) {
            // NEW: account-lockout handling — fetch lock expiry to build a
            // user-friendly "try again after X min Y sec" message
            LocalDateTime lockedUntil = customUserDetailsService.getLockedUntil(loginRequest.getUsername(), tenantId);

            String message = "Account is locked due to multiple failed login attempts. Please try again later.";
            if (lockedUntil != null) {
                Duration remaining = Duration.between(LocalDateTime.now(), lockedUntil);
                if (!remaining.isNegative()) {
                    long minutes = remaining.toMinutes();
                    long seconds = remaining.minusMinutes(minutes).getSeconds();
                    message = String.format(
                            "Account locked after %d failed login attempts. Try again after %d min %d sec.",
                            customUserDetailsService.getMaxAttempts(), minutes, seconds);
                }
            }
            // NEW: custom exception so controller/advice can return a distinct
            // "locked" response instead of a generic auth failure
            throw new AccountLockedException(message);
        } catch (BadCredentialsException ex) {
            // NEW: Wrong password -> track the failed attempt, then rethrow so the
            // existing "invalid credentials" behavior is unchanged for the caller.
            customUserDetailsService.incrementFailedAttempts(loginRequest.getUsername(), tenantId);
            throw ex;
        }

        // NEW: Successful login -> clear any prior failed-attempt count / lock state
        customUserDetailsService.resetFailedAttempts(loginRequest.getUsername(), tenantId);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String sessionId = sessionManagementService.createSession(
                loginRequest.getUsername(),
                tenantId
        );
        String accessToken = tokenProvider.generateAccessToken(authentication,sessionId);
        String refreshToken = tokenProvider.generateRefreshToken(loginRequest.getUsername(), tenantId,sessionId);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(loginRequest.getUsername())
                .tenantId(tenantId)
                .roles(roles)
                .sessionId(sessionId)
                .build();
    }

    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {

        // NEW: enforce password strength rules before creating the account
        passwordValidator.validate(registerRequest.getPassword());

        if (StringUtils.hasText(registerRequest.getTenantId())) {
            TenantContext.setTenantId(registerRequest.getTenantId());
        }
        String tenantId = TenantContext.getTenantId();

        if (customUserDetailsService.existsByUsernameAndTenant(registerRequest.getUsername(), tenantId)) {
            throw new BadRequestException(String.format(
                    "Username '%s' is already taken in tenant '%s'", registerRequest.getUsername(), tenantId));
        }

        List<String> roles = (registerRequest.getRoles() != null && !registerRequest.getRoles().isEmpty())
                ? registerRequest.getRoles()
                : List.of("ROLE_USER");

        customUserDetailsService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                roles,
                tenantId
        );

        // Auto-login after registration
        return login(new LoginRequestDTO(registerRequest.getUsername(), registerRequest.getPassword(), tenantId));
    }

    public AuthResponseDTO refreshToken(TokenRefreshRequestDTO refreshRequest) {
        String token = refreshRequest.getRefreshToken();
        if (!tokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String username = tokenProvider.getUsernameFromJWT(token);
        String tenantId = tokenProvider.getTenantIdFromJWT(token);
        TenantContext.setTenantId(tenantId);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        String newAccessToken = tokenProvider.generateAccessToken(
                username,
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")),
                tenantId
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(username, tenantId);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .username(username)
                .tenantId(tenantId)
                .roles(roles)
                .build();
    }
}
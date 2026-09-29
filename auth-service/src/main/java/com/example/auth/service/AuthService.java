package com.example.auth.service;

import com.example.auth.audit.service.AuditService;
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

    // Audit & Compliance
    private final AuditService AuditService;

    public AuthService(AuthenticationManager authenticationManager,
                       CustomUserDetailsService customUserDetailsService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       PasswordValidator passwordValidator,
                       AuditService AuditService) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.passwordValidator = passwordValidator;
        this.AuditService = AuditService;
    }

    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        if (StringUtils.hasText(loginRequest.getTenantId())) {
            TenantContext.setTenantId(loginRequest.getTenantId());
        }

        String tenantId = TenantContext.getTenantId();

        // Declared here (not inside try) since it's only assigned on success
        // but needed below
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (LockedException ex) {

            // NEW: account-lockout handling
            LocalDateTime lockedUntil =
                    customUserDetailsService.getLockedUntil(
                            loginRequest.getUsername(),
                            tenantId
                    );

            String message =
                    "Account is locked due to multiple failed login attempts. Please try again later.";

            if (lockedUntil != null) {
                Duration remaining =
                        Duration.between(LocalDateTime.now(), lockedUntil);

                if (!remaining.isNegative()) {
                    long minutes = remaining.toMinutes();
                    long seconds =
                            remaining.minusMinutes(minutes).getSeconds();

                    message = String.format(
                            "Account locked after %d failed login attempts. Try again after %d min %d sec.",
                            customUserDetailsService.getMaxAttempts(),
                            minutes,
                            seconds
                    );
                }
            }

            AuditService.accountLocked(
                    loginRequest.getUsername(),
                    tenantId
            );

            throw new AccountLockedException(message);

        } catch (BadCredentialsException ex) {

            // NEW: Wrong password -> track failed attempt
            customUserDetailsService.incrementFailedAttempts(
                    loginRequest.getUsername(),
                    tenantId
            );

            AuditService.loginFailed(
                    loginRequest.getUsername(),
                    tenantId
            );

            throw ex;
        }

        // NEW: Successful login -> clear prior failed-attempt state
        customUserDetailsService.resetFailedAttempts(
                loginRequest.getUsername(),
                tenantId
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // Audit successful login
        AuditService.loginSuccess(
                loginRequest.getUsername(),
                tenantId
        );

        // Existing token generation
        String accessToken = tokenProvider.generateAccessToken(
                loginRequest.getUsername(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")),
                tenantId
        );

        String refreshToken = tokenProvider.generateRefreshToken(
                loginRequest.getUsername(),
                tenantId
        );

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

                .build();
    }
    public AuthResponseDTO completeMfaLogin(String username, String tenantId) {

        if (StringUtils.hasText(tenantId)) {
            TenantContext.setTenantId(tenantId);
        }

        String effectiveTenantId = TenantContext.getTenantId();

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(username);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String sessionId = sessionManagementService.createSession(
                username,
                effectiveTenantId
        );

        String accessToken =
                tokenProvider.generateAccessToken(
                        authentication,
                        sessionId
                );

        String refreshToken =
                tokenProvider.generateRefreshToken(
                        username,
                        effectiveTenantId,
                        sessionId
                );

        List<String> roles =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(username)
                .tenantId(effectiveTenantId)
                .roles(roles)
                .sessionId(sessionId)
                .build();
    }

    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {

        // NEW: enforce password strength rules
        passwordValidator.validate(registerRequest.getPassword());

        if (StringUtils.hasText(registerRequest.getTenantId())) {
            TenantContext.setTenantId(registerRequest.getTenantId());
        }

        String tenantId = TenantContext.getTenantId();

        if (customUserDetailsService.existsByUsernameAndTenant(
                registerRequest.getUsername(),
                tenantId)) {

            throw new BadRequestException(String.format(
                    "Username '%s' is already taken in tenant '%s'",
                    registerRequest.getUsername(),
                    tenantId
            ));
        }

        List<String> roles =
                (registerRequest.getRoles() != null
                        && !registerRequest.getRoles().isEmpty())
                        ? registerRequest.getRoles()
                        : List.of("ROLE_USER");

        customUserDetailsService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                roles,
                tenantId
        );

        // Audit successful registration
        AuditService.userRegistered(
                registerRequest.getUsername(),
                tenantId
        );

        // Auto-login after registration
        return login(
                new LoginRequestDTO(
                        registerRequest.getUsername(),
                        registerRequest.getPassword(),
                        tenantId
                )
        );
    }

    public AuthResponseDTO refreshToken(
            TokenRefreshRequestDTO refreshRequest) {

        String token = refreshRequest.getRefreshToken();

        if (!tokenProvider.validateToken(token)) {
            throw new BadRequestException(
                    "Invalid or expired refresh token"
            );
        }

        String username = tokenProvider.getUsernameFromJWT(token);
        String tenantId = tokenProvider.getTenantIdFromJWT(token);

        TenantContext.setTenantId(tenantId);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(username);

        String newAccessToken = tokenProvider.generateAccessToken(
                username,
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")),
                tenantId
        );

        String newRefreshToken =
                tokenProvider.generateRefreshToken(
                        username,
                        tenantId
                );

        // Audit token refresh
        AuditService.tokenRefresh(
                username,
                tenantId
        );

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
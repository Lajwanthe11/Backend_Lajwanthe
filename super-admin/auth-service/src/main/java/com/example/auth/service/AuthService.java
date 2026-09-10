package com.example.auth.service;

import com.example.auth.dto.AuthResponseDTO;
import com.example.auth.dto.LoginRequestDTO;
import com.example.auth.dto.RegisterRequestDTO;
import com.example.auth.dto.TokenRefreshRequestDTO;
import com.example.auth.security.jwt.JwtTokenProvider;
import com.example.auth.security.user.CustomUserDetailsService;
import com.example.common.exception.BadRequestException;
import com.example.common.tenant.TenantContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private final PasswordValidator passwordValidator;

    public AuthService(AuthenticationManager authenticationManager,
                       CustomUserDetailsService customUserDetailsService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       PasswordValidator passwordValidator) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.passwordValidator = passwordValidator;
    }

    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        if (StringUtils.hasText(loginRequest.getTenantId())) {
            TenantContext.setTenantId(loginRequest.getTenantId());
        }
        String tenantId = TenantContext.getTenantId();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(loginRequest.getUsername(), tenantId);

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

    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {

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




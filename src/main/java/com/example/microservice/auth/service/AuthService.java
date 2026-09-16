package com.example.microservice.auth.service;

import com.example.microservice.auth.dto.AuthResponse;
import com.example.microservice.auth.dto.LoginRequest;
import com.example.microservice.auth.dto.RegisterRequest;
import com.example.microservice.auth.dto.TokenRefreshRequest;
import com.example.microservice.auth.security.jwt.JwtTokenProvider;
import com.example.microservice.auth.security.user.CustomUserDetailsService;
import com.example.microservice.auth.security.user.UserPrincipal;
import com.example.microservice.common.exception.BadRequestException;
import com.example.microservice.common.tenant.TenantContext;
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

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       CustomUserDetailsService customUserDetailsService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse login(LoginRequest loginRequest) {
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

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(loginRequest.getUsername())
                .tenantId(tenantId)
                .roles(roles)
                .build();
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        if (StringUtils.hasText(registerRequest.getTenantId())) {
            TenantContext.setTenantId(registerRequest.getTenantId());
        }
        String tenantId = TenantContext.getTenantId();

        if (customUserDetailsService.existsByUsernameAndTenant(registerRequest.getUsername(), tenantId)) {
            throw new BadRequestException(String.format("Username '%s' is already taken in tenant '%s'", registerRequest.getUsername(), tenantId));
        }

        List<String> roles = registerRequest.getRoles() != null && !registerRequest.getRoles().isEmpty()
                ? registerRequest.getRoles()
                : List.of("ROLE_USER");

        customUserDetailsService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                roles,
                tenantId
        );

        return login(new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword(), tenantId));
    }

    public AuthResponse refreshToken(TokenRefreshRequest refreshRequest) {
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

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .username(username)
                .tenantId(tenantId)
                .roles(roles)
                .build();
    }
}

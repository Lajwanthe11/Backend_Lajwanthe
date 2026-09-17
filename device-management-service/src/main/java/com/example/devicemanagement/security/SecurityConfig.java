package com.example.devicemanagement.security;

import com.example.common.security.jwt.CommonJwtAuthenticationFilter;
import com.example.common.security.jwt.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security config for device-management-service.
 *
 * Almost everything requires a valid JWT — the fine-grained distinction between
 * "any authenticated user may register their own device" and "only Super/Security
 * Administrators may view, trust, block, or remove devices" is enforced with
 * @PreAuthorize on individual controller methods, which is why @EnableMethodSecurity
 * is on.
 *
 * /devices/validate is the one deliberate exception: it's called by auth-service
 * during login, before the user has a JWT yet, so it's permitted here at the
 * filter-chain level (no @PreAuthorize would matter otherwise — the chain would
 * still reject the request before the controller method is ever reached).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CommonJwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            CommonJwtAuthenticationFilter jwtAuthenticationFilter) {

        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // REST API - disable CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // Return JSON 401 response for unauthorized requests
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )

                // JWT authentication is stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Spring error endpoint
                        .requestMatchers("/error").permitAll()

                        // Inter-service call from auth-service during login —
                        // no user JWT exists yet at this point in the flow
                        .requestMatchers("/devices/validate").permitAll()

                        // All other APIs require JWT; per-endpoint role checks via @PreAuthorize
                        .anyRequest().authenticated()
                )

                // Process JWT before Spring's username/password authentication
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
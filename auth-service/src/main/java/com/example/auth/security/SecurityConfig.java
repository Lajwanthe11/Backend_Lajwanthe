package com.example.auth.security;

import com.example.common.security.jwt.JwtAuthenticationEntryPoint;
import com.example.auth.security.jwt.JwtAuthenticationFilter;
import com.example.auth.security.oauth2.CustomOAuth2UserService;
import com.example.auth.security.oauth2.CustomOidcUserService;
import com.example.auth.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.auth.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.example.auth.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for the Auth Service.
 * - Stateless sessions (JWT-based)
 * - CSRF disabled (REST API)
 * - OAuth2 login for GitHub (plain OAuth2)
 * - OIDC login for Google / Microsoft
 * - Authorization requests stored in an HttpOnly cookie (not HttpSession),
 *   since this service runs fully stateless.
 * - Public: /auth/**, swagger, oauth2/**, and the static SSO demo pages
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomOAuth2UserService customOAuth2UserService,
                          CustomOidcUserService customOidcUserService,
                          OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                          OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                          HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository,
                          CorsConfigurationSource corsConfigurationSource) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public API endpoints — no authentication required
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()
                        // Public static assets + OAuth2 flow endpoints
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/callback.html",
                                "/app.js",
                                "/favicon.ico",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)       // GitHub (plain OAuth2)
                                .oidcUserService(customOidcUserService)     // Google / Microsoft (OIDC)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                );

        // Add JWT filter before username/password filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Auth Service — handles JWT token generation, OAuth2 login (Google/GitHub),
 * user registration, and token refresh.
 *
 * Runs on port 8081 (see application.yml).
 * Exposes POST /api/auth/validate for inter-service token introspection.
 *
 * scanBasePackages = "com.example" ensures all @Component / @Configuration
 * beans from common-lib (com.example.common.*) are discovered at runtime.
 */
@SpringBootApplication(scanBasePackages = "com.example")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

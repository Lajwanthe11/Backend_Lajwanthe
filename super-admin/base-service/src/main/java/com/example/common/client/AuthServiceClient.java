package com.example.common.client;

import com.example.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Shared HTTP client for calling the Auth Service from any downstream microservice.
 *
 * Primary use: POST /auth/validate — explicit JWT token introspection
 * (e.g., to verify the user still exists, or to check token revocation).
 *
 * NOTE: Per-request JWT validation still happens locally via JwtTokenValidator (no network).
 * This client is for supplemental/explicit inter-service calls only.
 *
 * Configure the auth service URL via: auth.service.url (or AUTH_SERVICE_URL env var).
 */
@Component
public class AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

    private final RestClient restClient;

    public AuthServiceClient(@Value("${auth.service.url:http://localhost:8081}") String authServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(authServiceUrl + "/api")
                .build();
    }

    /**
     * Validates a JWT token by calling the auth-service introspection endpoint.
     *
     * @param token the raw JWT token string (no "Bearer " prefix)
     * @return a Map with: valid(boolean), username, tenantId, roles, message
     */
    public Map<String, Object> validateToken(String token) {
        try {
            var response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/auth/validate")
                            .queryParam("token", token)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (RestClientException e) {
            log.error("Failed to call auth-service validate endpoint: {}", e.getMessage());
        }
        return Map.of("valid", false, "message", "Auth service unreachable");
    }

    /**
     * Checks whether the auth service is reachable and healthy.
     */
    public boolean isAuthServiceHealthy() {
        try {
            restClient.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.warn("Auth service health check failed: {}", e.getMessage());
            return false;
        }
    }
}

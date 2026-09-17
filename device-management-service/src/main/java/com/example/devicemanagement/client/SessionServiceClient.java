package com.example.devicemanagement.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SessionServiceClient {

    private static final Logger log = LoggerFactory.getLogger(SessionServiceClient.class);

    private final RestClient restClient;

    public SessionServiceClient(@Value("${session.service.url:http://localhost:8083}") String sessionServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(sessionServiceUrl + "/api")
                .build();
    }

    public void terminateSessionsForDevice(String deviceIdentifier) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sessions/terminate-by-device")
                            .queryParam("deviceIdentifier", deviceIdentifier)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Failed to terminate sessions for device '{}' via session-service: {}",
                    deviceIdentifier, e.getMessage());
        }
    }
}
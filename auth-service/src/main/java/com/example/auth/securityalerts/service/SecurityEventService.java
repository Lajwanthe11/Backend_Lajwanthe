package com.example.auth.securityalerts.service;

import com.example.auth.securityalerts.dto.SecurityEventFilter;
import com.example.auth.securityalerts.dto.SecurityEventIngestResponse;
import com.example.auth.securityalerts.dto.SecurityEventRequest;
import com.example.auth.securityalerts.dto.SecurityEventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Alert Monitoring Engine: the single entry point through which modules report security events.
 *
 * Each event is stored, run through threat detection, and checked against its alert policy,
 * which may raise a new alert or add the event to an alert that is still open.
 */
public interface SecurityEventService {

    /**
     * For modules inside auth-service (Login, MFA, Password Policy, Account Lockout, Session Management).
     * Runs in its own transaction and never throws: a monitoring failure is logged and must not break
     * the login or API call that reported the event. IP address and User-Agent default to the
     * current HTTP request's.
     */
    SecurityEventIngestResponse record(SecurityEventRequest request);

    /**
     * For other microservices, through POST /security-alerts/events/ingest. Checks the shared API
     * key and rejects invalid input with an error.
     */
    SecurityEventIngestResponse ingest(SecurityEventRequest request, String apiKey);

    Page<SecurityEventResponse> searchEvents(SecurityEventFilter filter, Pageable pageable);
}

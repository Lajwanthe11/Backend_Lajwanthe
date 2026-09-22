package com.example.auth.securityalerts.service;

import com.example.auth.securityalerts.dto.AlertActionRequest;
import com.example.auth.securityalerts.dto.ResolveAlertRequest;
import com.example.auth.securityalerts.dto.SecurityAlertFilter;
import com.example.auth.securityalerts.dto.SecurityAlertResponse;
import com.example.auth.securityalerts.dto.SecurityEventResponse;
import com.example.auth.securityalerts.dto.UserSecurityAlertResponse;
import com.example.auth.securityalerts.entity.SecurityAlert;
import com.example.auth.securityalerts.entity.SecurityAlertPolicy;
import com.example.auth.securityalerts.entity.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SecurityAlertService {

    // --- Used by the alert engine (SecurityEventService) ---

    /**
     * If an alert for the same tenant, event type and subject is still active and inside the
     * policy's suppression window, counts the event against it and returns it.
     */
    Optional<SecurityAlert> mergeIntoActiveAlert(SecurityEvent event, SecurityAlertPolicy policy);

    /** Raises a new alert. eventCount = events that crossed the threshold. */
    SecurityAlert createAlert(SecurityEvent event, SecurityAlertPolicy policy, long eventCount);

    // --- Alert list and handling ---

    Page<SecurityAlertResponse> searchAlerts(SecurityAlertFilter filter, Pageable pageable);

    SecurityAlertResponse getAlert(Long id);

    SecurityAlertResponse acknowledgeAlert(Long id, AlertActionRequest request);

    SecurityAlertResponse resolveAlert(Long id, ResolveAlertRequest request);

    SecurityAlertResponse closeAlert(Long id, AlertActionRequest request);

    /** The security events behind an alert. */
    Page<SecurityEventResponse> getAlertEvents(Long id, Pageable pageable);

    /** Alerts about the caller's own account. */
    Page<UserSecurityAlertResponse> getMyAlerts(Pageable pageable);
}

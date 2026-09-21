package com.example.auth.securityalerts.service.impl;

import com.example.auth.securityalerts.entity.SecurityAlertPolicy;
import com.example.auth.securityalerts.entity.SecurityEvent;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import com.example.auth.securityalerts.entity.SecurityEvent.SourceModule;
import com.example.auth.securityalerts.repository.SecurityEventRepository;
import com.example.auth.securityalerts.service.SecurityAlertPolicyService;
import com.example.auth.securityalerts.service.ThreatDetectionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Rules for the Session Management alerts. They work from sign-in events (LOGIN_SUCCESS from the
 * Login module, SESSION_CREATED from Session Management), so those modules only have to report
 * the sign-in.
 */
@Service
public class ThreatDetectionServiceImpl implements ThreatDetectionService {

    /** Events that say where a user has signed in. */
    private static final List<EventType> SIGN_IN_EVENTS = List.of(EventType.LOGIN_SUCCESS, EventType.SESSION_CREATED);

    private final SecurityEventRepository eventRepository;
    private final SecurityAlertPolicyService policyService;

    public ThreatDetectionServiceImpl(SecurityEventRepository eventRepository,
                                      SecurityAlertPolicyService policyService) {
        this.eventRepository = eventRepository;
        this.policyService = policyService;
    }

    @Override
    public List<SecurityEvent> detect(SecurityEvent event) {
        if (!SIGN_IN_EVENTS.contains(event.getEventType()) || !StringUtils.hasText(event.getUsername())) {
            return List.of();
        }
        return detectMultipleIpAddresses(event).map(List::of).orElse(List.of());
    }

    /**
     * MULTIPLE_IP_SESSIONS: the user signed in from at least {threshold} different IP addresses
     * within the policy's time window, e.g. shared credentials or a hijacked session.
     */
    private Optional<SecurityEvent> detectMultipleIpAddresses(SecurityEvent event) {
        if (!StringUtils.hasText(event.getIpAddress())) {
            return Optional.empty();
        }
        Optional<SecurityAlertPolicy> policy = policyService.resolveActivePolicy(event.getTenantId(), EventType.MULTIPLE_IP_SESSIONS);
        if (policy.isEmpty()) {
            return Optional.empty();
        }
        int windowMinutes = policy.get().getTimeWindowMinutes();
        Set<String> addresses = new TreeSet<>(eventRepository.findDistinctIpAddresses(
                event.getTenantId(), event.getUsername(), SIGN_IN_EVENTS, event.getOccurredAt().minusMinutes(windowMinutes)));
        addresses.add(event.getIpAddress());
        if (addresses.size() < Math.max(2, policy.get().getThreshold())) {
            return Optional.empty();
        }
        return Optional.of(derive(event, EventType.MULTIPLE_IP_SESSIONS,
                addresses.size() + " different IP addresses within " + windowMinutes + " minutes: " + String.join(", ", addresses)));
    }

    private static SecurityEvent derive(SecurityEvent source, EventType type, String details) {
        SecurityEvent event = new SecurityEvent();
        event.setTenantId(source.getTenantId());
        event.setEventType(type);
        event.setSourceModule(SourceModule.THREAT_DETECTION);
        event.setSubjectKey(source.getSubjectKey());
        event.setUserId(source.getUserId());
        event.setUsername(source.getUsername());
        event.setCompanyId(source.getCompanyId());
        event.setCompanyName(source.getCompanyName());
        event.setDepartmentId(source.getDepartmentId());
        event.setDepartmentName(source.getDepartmentName());
        event.setIpAddress(source.getIpAddress());
        event.setDeviceId(source.getDeviceId());
        event.setDeviceName(source.getDeviceName());
        event.setUserAgent(source.getUserAgent());
        event.setLocation(source.getLocation());
        event.setSessionId(source.getSessionId());
        event.setDetails(details.length() > 2000 ? details.substring(0, 2000) : details);
        event.setOccurredAt(source.getOccurredAt());
        event.setReceivedAt(source.getReceivedAt());
        return event;
    }
}

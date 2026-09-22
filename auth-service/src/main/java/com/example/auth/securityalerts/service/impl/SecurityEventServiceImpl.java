package com.example.auth.securityalerts.service.impl;

import com.example.auth.securityalerts.dto.DateRange;
import com.example.auth.securityalerts.dto.SecurityEventFilter;
import com.example.auth.securityalerts.dto.SecurityEventIngestResponse;
import com.example.auth.securityalerts.dto.SecurityEventIngestResponse.RaisedAlert;
import com.example.auth.securityalerts.dto.SecurityEventRequest;
import com.example.auth.securityalerts.dto.SecurityEventResponse;
import com.example.auth.securityalerts.entity.SecurityAlert;
import com.example.auth.securityalerts.entity.SecurityAlertPolicy;
import com.example.auth.securityalerts.entity.SecurityEvent;
import com.example.auth.securityalerts.entity.SecurityEvent.SourceModule;
import com.example.auth.securityalerts.repository.SecurityEventRepository;
import com.example.auth.securityalerts.service.SecurityAlertAccessService;
import com.example.auth.securityalerts.service.SecurityAlertPolicyService;
import com.example.auth.securityalerts.service.SecurityAlertService;
import com.example.auth.securityalerts.service.SecurityEventService;
import com.example.auth.securityalerts.service.ThreatDetectionService;
import com.example.common.exception.AppException;
import com.example.common.exception.BadRequestException;
import com.example.common.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class SecurityEventServiceImpl implements SecurityEventService {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventServiceImpl.class);

    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$");
    /** Hex digits, colons and dots only, starting with a hex digit or colon: InetAddress then parses it as a literal and never does a DNS lookup. */
    private static final Pattern IPV6_CHARACTERS = Pattern.compile("^[0-9A-Fa-f:][0-9A-Fa-f:.]*$");

    /** How far in the future a source clock may be before occurredAt is rejected. */
    private static final int MAX_CLOCK_SKEW_MINUTES = 5;

    private final SecurityEventRepository eventRepository;
    private final SecurityAlertPolicyService policyService;
    private final SecurityAlertService alertService;
    private final ThreatDetectionService threatDetectionService;
    private final SecurityAlertAccessService access;
    private final TransactionTemplate newTransaction;
    private final boolean monitoringEnabled;
    private final String ingestApiKey;

    public SecurityEventServiceImpl(SecurityEventRepository eventRepository,
                                    SecurityAlertPolicyService policyService,
                                    SecurityAlertService alertService,
                                    ThreatDetectionService threatDetectionService,
                                    SecurityAlertAccessService access,
                                    PlatformTransactionManager transactionManager,
                                    @Value("${app.security-alerts.enabled:true}") boolean monitoringEnabled,
                                    @Value("${app.security-alerts.ingest-api-key:}") String ingestApiKey) {
        this.eventRepository = eventRepository;
        this.policyService = policyService;
        this.alertService = alertService;
        this.threatDetectionService = threatDetectionService;
        this.access = access;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.monitoringEnabled = monitoringEnabled;
        this.ingestApiKey = ingestApiKey;
    }

    // ---------------------------------------------------------------
    // Entry points
    // ---------------------------------------------------------------

    @Override
    public SecurityEventIngestResponse record(SecurityEventRequest request) {
        if (!monitoringEnabled || request == null) {
            return SecurityEventIngestResponse.disabled();
        }
        try {
            return process(request, access.currentRequest().orElse(null));
        } catch (RuntimeException ex) {
            log.error("Could not record security event {} for user '{}': {}",
                    request.getEventType(), request.getUsername(), ex.getMessage(), ex);
            return new SecurityEventIngestResponse(null, true, List.of());
        }
    }

    @Override
    public SecurityEventIngestResponse ingest(SecurityEventRequest request, String apiKey) {
        verifyApiKey(apiKey);
        if (!monitoringEnabled) {
            return SecurityEventIngestResponse.disabled();
        }
        // No request fallback here: the HTTP caller is the reporting service, not the user.
        return process(request, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SecurityEventResponse> searchEvents(SecurityEventFilter filter, Pageable pageable) {
        String tenantId = access.resolveTenantScope(filter.getTenantId());
        DateRange range = DateRange.of(filter.getFrom(), filter.getTo());
        return eventRepository.findAll(
                        SecurityEventRepository.matching(tenantId, filter, range.from(), range.toExclusive()), pageable)
                .map(SecurityEventResponse::from);
    }

    // ---------------------------------------------------------------
    // Engine
    // ---------------------------------------------------------------

    /**
     * Own transaction (REQUIRES_NEW): the event is kept even when the caller's transaction rolls
     * back, which is exactly what happens around a failed login.
     */
    private SecurityEventIngestResponse process(SecurityEventRequest request, HttpServletRequest httpRequest) {
        SecurityEvent event = toEvent(request, httpRequest);
        return newTransaction.execute(status -> {
            // Detection looks at the history before this event is saved.
            List<SecurityEvent> derivedEvents = threatDetectionService.detect(event);

            List<RaisedAlert> alerts = new ArrayList<>();
            SecurityEvent saved = eventRepository.save(event);
            evaluate(saved).ifPresent(alerts::add);

            for (SecurityEvent derived : derivedEvents) {
                derived.setDerivedFromEventId(saved.getId());
                evaluate(eventRepository.save(derived)).ifPresent(alerts::add);
            }
            return new SecurityEventIngestResponse(saved.getId(), true, alerts);
        });
    }

    /**
     * Applies the event's policy: join a still-open alert for the same subject, or raise a new
     * alert once the number of not-yet-alerted events in the time window reaches the threshold.
     */
    private Optional<RaisedAlert> evaluate(SecurityEvent event) {
        Optional<SecurityAlertPolicy> activePolicy = policyService.resolveActivePolicy(event.getTenantId(), event.getEventType());
        if (activePolicy.isEmpty()) {
            return Optional.empty();
        }
        SecurityAlertPolicy policy = activePolicy.get();

        Optional<SecurityAlert> merged = alertService.mergeIntoActiveAlert(event, policy);
        if (merged.isPresent()) {
            event.setAlertId(merged.get().getId());
            return Optional.of(raised(event, merged.get(), false));
        }

        LocalDateTime windowStart = event.getOccurredAt().minusMinutes(policy.getTimeWindowMinutes());
        long eventCount = 1;
        if (event.getEventType().isCountThreshold() && policy.getThreshold() > 1) {
            eventCount = eventRepository.countByTenantIdAndEventTypeAndSubjectKeyAndOccurredAtGreaterThanEqualAndAlertIdIsNull(
                    event.getTenantId(), event.getEventType(), event.getSubjectKey(), windowStart);
            if (eventCount < policy.getThreshold()) {
                return Optional.empty();
            }
        }

        SecurityAlert alert = alertService.createAlert(event, policy, eventCount);
        event.setAlertId(alert.getId());
        if (eventCount > 1) {
            // The earlier events that counted towards the threshold become the alert's evidence.
            eventRepository.linkToAlert(alert.getId(), event.getTenantId(), event.getEventType(),
                    event.getSubjectKey(), windowStart);
        }
        return Optional.of(raised(event, alert, true));
    }

    private static RaisedAlert raised(SecurityEvent event, SecurityAlert alert, boolean newAlert) {
        return new RaisedAlert(event.getId(), event.getEventType(), alert.getId(), alert.getAlertCode(),
                alert.getSeverity(), newAlert);
    }

    // ---------------------------------------------------------------
    // Validation and mapping
    // ---------------------------------------------------------------

    private void verifyApiKey(String apiKey) {
        if (!StringUtils.hasText(ingestApiKey)) {
            throw new AppException("Security event ingestion is disabled: no API key is configured "
                    + "(app.security-alerts.ingest-api-key / SECURITY_ALERTS_INGEST_API_KEY)", HttpStatus.FORBIDDEN);
        }
        if (!StringUtils.hasText(apiKey) || !MessageDigest.isEqual(
                ingestApiKey.getBytes(StandardCharsets.UTF_8), apiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new AppException("Missing or invalid X-Internal-Api-Key header", HttpStatus.UNAUTHORIZED);
        }
    }

    private SecurityEvent toEvent(SecurityEventRequest request, HttpServletRequest httpRequest) {
        if (request.getEventType() == null) {
            throw new BadRequestException("eventType is required");
        }
        String username = trimToNull(request.getUsername());
        String ipAddress = trimToNull(request.getIpAddress());
        String userAgent = trimToNull(request.getUserAgent());
        if (httpRequest != null) {
            if (ipAddress == null) {
                ipAddress = stripZoneId(access.clientIp(httpRequest));
            }
            if (userAgent == null) {
                userAgent = trimToNull(httpRequest.getHeader("User-Agent"));
            }
        }
        if (ipAddress != null && !isValidIpAddress(ipAddress)) {
            throw new BadRequestException("ipAddress is not a valid IPv4 or IPv6 address: " + ipAddress);
        }
        if (username == null && ipAddress == null) {
            throw new BadRequestException("A security event needs a username or an ipAddress");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = request.getOccurredAt() != null ? request.getOccurredAt() : now;
        if (occurredAt.isAfter(now.plusMinutes(MAX_CLOCK_SKEW_MINUTES))) {
            throw new BadRequestException("occurredAt cannot be in the future");
        }

        String deviceId = trimToNull(request.getDeviceId());
        String tenantId = trimToNull(request.getTenantId());

        SecurityEvent event = new SecurityEvent();
        event.setTenantId(tenantId != null ? truncate(tenantId, 100) : TenantContext.getTenantId());
        event.setEventType(request.getEventType());
        event.setSourceModule(request.getSourceModule() != null ? request.getSourceModule() : SourceModule.OTHER);
        event.setSubjectKey(username != null ? truncate(username, 150) : "ip:" + ipAddress);
        event.setUserId(truncate(trimToNull(request.getUserId()), 100));
        event.setUsername(truncate(username, 150));
        event.setCompanyId(request.getCompanyId());
        event.setCompanyName(truncate(trimToNull(request.getCompanyName()), 200));
        event.setDepartmentId(request.getDepartmentId());
        event.setDepartmentName(truncate(trimToNull(request.getDepartmentName()), 200));
        event.setIpAddress(ipAddress);
        event.setDeviceId(truncate(deviceId, 200));
        event.setDeviceName(truncate(trimToNull(request.getDeviceName()), 200));
        event.setUserAgent(truncate(userAgent, 500));
        event.setLocation(truncate(trimToNull(request.getLocation()), 200));
        event.setSessionId(truncate(trimToNull(request.getSessionId()), 200));
        event.setDetails(truncate(trimToNull(request.getDetails()), 2000));
        event.setOccurredAt(occurredAt);
        event.setReceivedAt(now);
        return event;
    }

    private static boolean isValidIpAddress(String value) {
        if (value.length() > 64) {
            return false;
        }
        if (IPV4.matcher(value).matches()) {
            return true;
        }
        if (value.indexOf(':') < 0 || !IPV6_CHARACTERS.matcher(value).matches()) {
            return false;
        }
        try {
            return InetAddress.getByName(value) instanceof Inet6Address;
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    /** "fe80::1%eth0" -> "fe80::1" */
    private static String stripZoneId(String address) {
        if (address == null) {
            return null;
        }
        int percent = address.indexOf('%');
        return percent >= 0 ? address.substring(0, percent) : address;
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static String truncate(String value, int maxLength) {
        return value != null && value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}

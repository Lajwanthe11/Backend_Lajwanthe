package com.example.auth.securityalerts.service.impl;

import com.example.auth.securityalerts.dto.AlertActionRequest;
import com.example.auth.securityalerts.dto.DateRange;
import com.example.auth.securityalerts.dto.ResolveAlertRequest;
import com.example.auth.securityalerts.dto.SecurityAlertFilter;
import com.example.auth.securityalerts.dto.SecurityAlertResponse;
import com.example.auth.securityalerts.dto.SecurityEventResponse;
import com.example.auth.securityalerts.dto.UserSecurityAlertResponse;
import com.example.auth.securityalerts.entity.SecurityAlert;
import com.example.auth.securityalerts.entity.SecurityAlert.ResolutionStatus;
import com.example.auth.securityalerts.entity.SecurityAlert.Status;
import com.example.auth.securityalerts.entity.SecurityAlertPolicy;
import com.example.auth.securityalerts.entity.SecurityEvent;
import com.example.auth.securityalerts.repository.SecurityAlertRepository;
import com.example.auth.securityalerts.repository.SecurityEventRepository;
import com.example.auth.securityalerts.service.SecurityAlertAccessService;
import com.example.auth.securityalerts.service.SecurityAlertAccessService.Actor;
import com.example.auth.securityalerts.service.SecurityAlertService;
import com.example.common.exception.AppException;
import com.example.common.exception.BadRequestException;
import com.example.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SecurityAlertServiceImpl implements SecurityAlertService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAlertServiceImpl.class);

    private static final List<Status> ACTIVE_STATUSES = List.of(Status.OPEN, Status.ACKNOWLEDGED);

    /** Critical alerts are prioritized: most severe first, newest first within a severity. */
    private static final Sort PRIORITY_SORT = Sort.by(Sort.Order.desc("severityRank"), Sort.Order.desc("alertTime"));

    private final SecurityAlertRepository alertRepository;
    private final SecurityEventRepository eventRepository;
    private final SecurityAlertAccessService access;

    public SecurityAlertServiceImpl(SecurityAlertRepository alertRepository,
                                    SecurityEventRepository eventRepository,
                                    SecurityAlertAccessService access) {
        this.alertRepository = alertRepository;
        this.eventRepository = eventRepository;
        this.access = access;
    }

    // ---------------------------------------------------------------
    // Alert engine
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public Optional<SecurityAlert> mergeIntoActiveAlert(SecurityEvent event, SecurityAlertPolicy policy) {
        if (policy.getSuppressionMinutes() <= 0) {
            return Optional.empty();
        }
        return alertRepository
                .findFirstByTenantIdAndEventTypeAndSubjectKeyAndStatusInAndLastOccurredAtGreaterThanEqualOrderByLastOccurredAtDesc(
                        event.getTenantId(), event.getEventType(), event.getSubjectKey(), ACTIVE_STATUSES,
                        event.getOccurredAt().minusMinutes(policy.getSuppressionMinutes()))
                .map(alert -> {
                    alert.setOccurrenceCount(alert.getOccurrenceCount() + 1);
                    if (event.getOccurredAt().isAfter(alert.getLastOccurredAt())) {
                        alert.setLastOccurredAt(event.getOccurredAt());
                    }
                    alert.setUpdatedBy(SecurityAlertAccessService.SYSTEM_USER);
                    return alertRepository.save(alert);
                });
    }

    @Override
    @Transactional
    public SecurityAlert createAlert(SecurityEvent event, SecurityAlertPolicy policy, long eventCount) {
        SecurityAlert alert = new SecurityAlert();
        alert.setTenantId(event.getTenantId());
        alert.setAlertType(policy.getAlertType());
        alert.setEventType(event.getEventType());
        alert.setSeverity(policy.getSeverity());
        alert.setStatus(Status.OPEN);
        alert.setResolutionStatus(ResolutionStatus.PENDING);
        alert.setTitle(event.getEventType().getTitle());
        alert.setDescription(describe(event, policy, eventCount));
        alert.setSourceModule(event.getSourceModule());
        alert.setPolicyId(policy.getId());
        alert.setSubjectKey(event.getSubjectKey());
        alert.setAlertTime(LocalDateTime.now());
        alert.setLastOccurredAt(event.getOccurredAt());
        alert.setOccurrenceCount((int) Math.max(1, eventCount));
        alert.setUserId(event.getUserId());
        alert.setUsername(event.getUsername());
        alert.setCompanyId(event.getCompanyId());
        alert.setCompanyName(event.getCompanyName());
        alert.setDepartmentId(event.getDepartmentId());
        alert.setDepartmentName(event.getDepartmentName());
        alert.setDeviceId(event.getDeviceId());
        alert.setDeviceName(event.getDeviceName());
        alert.setIpAddress(event.getIpAddress());
        alert.setLocation(event.getLocation());
        alert.setCreatedBy(SecurityAlertAccessService.SYSTEM_USER);
        alert.setUpdatedBy(SecurityAlertAccessService.SYSTEM_USER);

        SecurityAlert saved = alertRepository.save(alert);
        saved.setAlertCode(String.format("SA-%06d", saved.getId()));

        log.info("Security alert {} raised: {} {} for '{}' in tenant '{}'",
                saved.getAlertCode(), saved.getSeverity(), saved.getEventType(), saved.getSubjectKey(), saved.getTenantId());
        return saved;
    }

    // ---------------------------------------------------------------
    // Alert list and handling
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<SecurityAlertResponse> searchAlerts(SecurityAlertFilter filter, Pageable pageable) {
        return alertRepository.findAll(specification(filter), withAlertSort(pageable, PRIORITY_SORT))
                .map(SecurityAlertResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityAlertResponse getAlert(Long id) {
        return SecurityAlertResponse.from(findVisibleAlert(id));
    }

    @Override
    @Transactional
    public SecurityAlertResponse acknowledgeAlert(Long id, AlertActionRequest request) {
        SecurityAlert alert = findVisibleAlert(id);
        if (alert.getStatus() != Status.OPEN) {
            throw conflict("Only OPEN alerts can be acknowledged; this alert is " + alert.getStatus());
        }
        alert.setStatus(Status.ACKNOWLEDGED);
        alert.setAcknowledgedBy(access.currentActor().username());
        alert.setAcknowledgedAt(LocalDateTime.now());
        return SecurityAlertResponse.from(save(alert));
    }

    @Override
    @Transactional
    public SecurityAlertResponse resolveAlert(Long id, ResolveAlertRequest request) {
        SecurityAlert alert = findVisibleAlert(id);
        if (!alert.getStatus().isActive()) {
            throw conflict("A " + alert.getStatus() + " alert cannot be resolved");
        }
        alert.setStatus(Status.RESOLVED);
        alert.setResolutionStatus(ResolutionStatus.RESOLVED);
        alert.setResolutionRemarks(request.resolutionRemarks().trim());
        alert.setResolutionTime(LocalDateTime.now());
        alert.setResolvedBy(access.currentActor().username());
        return SecurityAlertResponse.from(save(alert));
    }

    @Override
    @Transactional
    public SecurityAlertResponse closeAlert(Long id, AlertActionRequest request) {
        SecurityAlert alert = findVisibleAlert(id);
        if (alert.getStatus() == Status.CLOSED) {
            throw conflict("This alert is already closed");
        }
        String remarks = request != null && StringUtils.hasText(request.remarks()) ? request.remarks().trim() : null;
        if (remarks == null && !StringUtils.hasText(alert.getResolutionRemarks())) {
            throw new BadRequestException("Resolution comments are mandatory before closing an alert");
        }

        Actor actor = access.currentActor();
        LocalDateTime now = LocalDateTime.now();
        if (alert.getStatus() != Status.RESOLVED) {
            // Closed without a separate resolve step, e.g. a false positive.
            alert.setResolutionStatus(ResolutionStatus.RESOLVED);
            alert.setResolutionTime(now);
            alert.setResolvedBy(actor.username());
        }
        if (!StringUtils.hasText(alert.getResolutionRemarks())) {
            alert.setResolutionRemarks(remarks);
        }
        alert.setStatus(Status.CLOSED);
        alert.setClosedBy(actor.username());
        alert.setClosedAt(now);
        return SecurityAlertResponse.from(save(alert));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SecurityEventResponse> getAlertEvents(Long id, Pageable pageable) {
        findVisibleAlert(id);
        return eventRepository.findByAlertId(id, pageable).map(SecurityEventResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSecurityAlertResponse> getMyAlerts(Pageable pageable) {
        Actor actor = access.currentActor();
        return alertRepository.findByTenantIdAndUsername(actor.tenantId(), actor.username(),
                        withAlertSort(pageable, Sort.by(Sort.Direction.DESC, "alertTime")))
                .map(UserSecurityAlertResponse::from);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Specification<SecurityAlert> specification(SecurityAlertFilter filter) {
        String tenantId = access.resolveTenantScope(filter.getTenantId());
        DateRange range = DateRange.of(filter.getFrom(), filter.getTo());
        return SecurityAlertRepository.matching(tenantId, filter, range.from(), range.toExclusive());
    }

    /** Another tenant's alert answers 404, the same as a missing one. */
    private SecurityAlert findVisibleAlert(Long id) {
        return alertRepository.findById(id)
                .filter(alert -> access.canAccessTenant(alert.getTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Security alert", "id", id));
    }

    private SecurityAlert save(SecurityAlert alert) {
        alert.setUpdatedBy(access.currentActor().username());
        try {
            return alertRepository.saveAndFlush(alert);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw conflict("This alert was changed by someone else in the meantime. Refresh it and try again.");
        }
    }

    private static AppException conflict(String message) {
        return new AppException(message, HttpStatus.CONFLICT);
    }

    /** Sorting by "severity" means by rank; the enum name would sort HIGH before LOW before MEDIUM. */
    private static Pageable withAlertSort(Pageable pageable, Sort defaultSort) {
        Sort sort = pageable.getSort().isUnsorted()
                ? defaultSort
                : Sort.by(pageable.getSort().stream()
                        .map(order -> "severity".equals(order.getProperty()) ? order.withProperty("severityRank") : order)
                        .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private static String describe(SecurityEvent event, SecurityAlertPolicy policy, long eventCount) {
        String subject = event.getUsername() != null
                ? "user '" + event.getUsername() + "'"
                : "IP address " + event.getIpAddress();
        StringBuilder text = new StringBuilder(event.getEventType().getTitle()).append(" for ").append(subject);
        if (eventCount > 1) {
            text.append(": ").append(eventCount).append(' ').append(event.getEventType())
                    .append(" events within ").append(policy.getTimeWindowMinutes()).append(" minutes");
        }
        text.append('.');
        if (StringUtils.hasText(event.getDetails())) {
            text.append(' ').append(event.getDetails());
        }
        return text.length() > 2000 ? text.substring(0, 2000) : text.toString();
    }
}

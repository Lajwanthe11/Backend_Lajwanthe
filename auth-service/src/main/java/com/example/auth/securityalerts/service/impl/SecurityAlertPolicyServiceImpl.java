package com.example.auth.securityalerts.service.impl;

import com.example.auth.securityalerts.dto.SecurityAlertPolicyRequest;
import com.example.auth.securityalerts.dto.SecurityAlertPolicyResponse;
import com.example.auth.securityalerts.entity.SecurityAlertPolicy;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import com.example.auth.securityalerts.repository.SecurityAlertPolicyRepository;
import com.example.auth.securityalerts.service.SecurityAlertAccessService;
import com.example.auth.securityalerts.service.SecurityAlertAccessService.Actor;
import com.example.auth.securityalerts.service.SecurityAlertPolicyService;
import com.example.common.exception.AppException;
import com.example.common.exception.BadRequestException;
import com.example.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class SecurityAlertPolicyServiceImpl implements SecurityAlertPolicyService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAlertPolicyServiceImpl.class);

    private static final int DEFAULT_SUPPRESSION_MINUTES = 60;

    private final SecurityAlertPolicyRepository repository;
    private final SecurityAlertAccessService access;

    public SecurityAlertPolicyServiceImpl(SecurityAlertPolicyRepository repository,
                                          SecurityAlertAccessService access) {
        this.repository = repository;
        this.access = access;
    }

    /** "Alert policies shall be configured": create the platform default for every event type that has none yet. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDefaultPolicies() {
        int created = 0;
        for (EventType type : EventType.values()) {
            if (!repository.existsByTenantIdIsNullAndEventType(type)) {
                repository.save(defaultPolicy(type));
                created++;
            }
        }
        if (created > 0) {
            log.info("Created {} default security alert policies", created);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityAlertPolicyResponse> getPolicies(String tenantId, EventType eventType) {
        Actor actor = access.currentActor();
        String scope = access.resolveTenantScope(tenantId);
        List<SecurityAlertPolicy> policies = actor.isSuperAdmin() && scope == null
                ? repository.findAllOrdered()
                : repository.findGlobalAndTenant(scope);
        return policies.stream()
                .filter(policy -> eventType == null || policy.getEventType() == eventType)
                .map(SecurityAlertPolicyResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityAlertPolicyResponse getPolicy(Long id) {
        SecurityAlertPolicy policy = repository.findById(id)
                .filter(p -> p.isGlobal() || access.canAccessTenant(p.getTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Security alert policy", "id", id));
        return SecurityAlertPolicyResponse.from(policy);
    }

    @Override
    @Transactional
    public SecurityAlertPolicyResponse createPolicy(SecurityAlertPolicyRequest request) {
        String tenantId = targetTenant(request, access.currentActor());
        EventType type = request.eventType();

        boolean exists = tenantId == null
                ? repository.existsByTenantIdIsNullAndEventType(type)
                : repository.findByTenantIdAndEventType(tenantId, type).isPresent();
        if (exists) {
            throw new AppException("A policy for " + type + " already exists for "
                    + (tenantId == null ? "the platform" : "organization '" + tenantId + "'")
                    + ". Update that policy instead.", HttpStatus.CONFLICT);
        }

        // A tenant override starts from the current platform default, so only the fields the
        // administrator sends differ from it.
        SecurityAlertPolicy base = tenantId == null
                ? defaultPolicy(type)
                : repository.findByTenantIdIsNullAndEventType(type).orElseGet(() -> defaultPolicy(type));

        SecurityAlertPolicy policy = new SecurityAlertPolicy();
        policy.setTenantId(tenantId);
        policy.setEventType(type);
        apply(policy, request, base);
        return SecurityAlertPolicyResponse.from(repository.save(policy));
    }

    @Override
    @Transactional
    public SecurityAlertPolicyResponse updatePolicy(Long id, SecurityAlertPolicyRequest request) {
        SecurityAlertPolicy policy = findManageablePolicy(id);
        if (request.eventType() != policy.getEventType()) {
            throw new BadRequestException("eventType of a policy cannot be changed; create a policy for "
                    + request.eventType() + " instead");
        }
        apply(policy, request, policy);
        return SecurityAlertPolicyResponse.from(repository.save(policy));
    }

    @Override
    @Transactional
    public SecurityAlertPolicyResponse setPolicyEnabled(Long id, boolean enabled) {
        SecurityAlertPolicy policy = findManageablePolicy(id);
        if (policy.isEnabled() != enabled) {
            policy.setEnabled(enabled);
            policy = repository.save(policy);
        }
        return SecurityAlertPolicyResponse.from(policy);
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        SecurityAlertPolicy policy = findManageablePolicy(id);
        if (policy.isGlobal()) {
            throw new BadRequestException("Platform default policies cannot be deleted; disable them instead");
        }
        repository.delete(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SecurityAlertPolicy> resolveActivePolicy(String tenantId, EventType eventType) {
        return repository.findByTenantIdAndEventType(tenantId, eventType)
                .or(() -> repository.findByTenantIdIsNullAndEventType(eventType))
                .filter(SecurityAlertPolicy::isEnabled);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** null = platform-wide. */
    private String targetTenant(SecurityAlertPolicyRequest request, Actor actor) {
        if (Boolean.TRUE.equals(request.global())) {
            if (!actor.isSuperAdmin()) {
                throw new AccessDeniedException("Only a Super Administrator can manage platform-wide policies");
            }
            return null;
        }
        if (StringUtils.hasText(request.tenantId())) {
            String requested = request.tenantId().trim();
            if (!actor.isSuperAdmin() && !requested.equals(actor.tenantId())) {
                throw new AccessDeniedException("You can only manage policies of your own organization");
            }
            return requested;
        }
        return actor.tenantId();
    }

    private SecurityAlertPolicy findManageablePolicy(Long id) {
        SecurityAlertPolicy policy = repository.findById(id)
                .filter(p -> p.isGlobal() || access.canAccessTenant(p.getTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Security alert policy", "id", id));
        if (policy.isGlobal() && !access.currentActor().isSuperAdmin()) {
            throw new AccessDeniedException("Only a Super Administrator can change platform-wide policies; "
                    + "create a policy for your organization to override it");
        }
        return policy;
    }

    /** Copies the request onto the policy; fields the request leaves null take the value from base. */
    private static void apply(SecurityAlertPolicy policy, SecurityAlertPolicyRequest request, SecurityAlertPolicy base) {
        policy.setName(request.name().trim());
        policy.setDescription(request.description() != null ? request.description() : base.getDescription());
        policy.setAlertType(request.alertType() != null ? request.alertType() : base.getAlertType());
        policy.setSeverity(request.severity());
        policy.setThreshold(request.threshold() != null ? request.threshold() : base.getThreshold());
        policy.setTimeWindowMinutes(request.timeWindowMinutes() != null ? request.timeWindowMinutes() : base.getTimeWindowMinutes());
        policy.setSuppressionMinutes(request.suppressionMinutes() != null ? request.suppressionMinutes() : base.getSuppressionMinutes());
        policy.setEnabled(request.enabled() != null ? request.enabled() : base.isEnabled());
    }

    private static SecurityAlertPolicy defaultPolicy(EventType type) {
        SecurityAlertPolicy policy = new SecurityAlertPolicy();
        policy.setName(type.getTitle());
        policy.setDescription("Platform default policy for " + type.name());
        policy.setEventType(type);
        policy.setAlertType(type.getAlertType());
        policy.setSeverity(type.getDefaultSeverity());
        policy.setThreshold(type.getDefaultThreshold());
        policy.setTimeWindowMinutes(type.getDefaultWindowMinutes());
        policy.setSuppressionMinutes(DEFAULT_SUPPRESSION_MINUTES);
        policy.setEnabled(type.isAlertByDefault());
        return policy;
    }
}

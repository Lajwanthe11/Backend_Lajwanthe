package com.example.auth.securityalerts.service;

import com.example.auth.securityalerts.dto.SecurityAlertPolicyRequest;
import com.example.auth.securityalerts.dto.SecurityAlertPolicyResponse;
import com.example.auth.securityalerts.entity.SecurityAlertPolicy;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;

import java.util.List;
import java.util.Optional;

/** Alert Policy Manager. */
public interface SecurityAlertPolicyService {

    /** Platform defaults plus the tenant's own policies. A Super Administrator without a tenant gets every policy. */
    List<SecurityAlertPolicyResponse> getPolicies(String tenantId, EventType eventType);

    SecurityAlertPolicyResponse getPolicy(Long id);

    SecurityAlertPolicyResponse createPolicy(SecurityAlertPolicyRequest request);

    SecurityAlertPolicyResponse updatePolicy(Long id, SecurityAlertPolicyRequest request);

    SecurityAlertPolicyResponse setPolicyEnabled(Long id, boolean enabled);

    void deletePolicy(Long id);

    /**
     * The policy that applies to an event: the tenant's own policy if it has one (even a disabled
     * one), otherwise the platform default. Empty when that policy is disabled or missing.
     */
    Optional<SecurityAlertPolicy> resolveActivePolicy(String tenantId, EventType eventType);
}

package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.entity.SecurityEvent;
import com.example.platformadmin.rbac.enums.SecurityEventType;
import com.example.platformadmin.rbac.repository.SecurityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

// Every denied authorization should create one audit/security event in this implementation.
@Service
public class SecurityEventLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventLogger.class);

    private final SecurityEventRepository repository;

    public SecurityEventLogger(SecurityEventRepository repository) {
        this.repository = repository;
    }

    public void logAccessDenied(String userId, String tenantId, String requestedPermission,
            String endpoint, String ipAddress) {
        SecurityEvent event = new SecurityEvent(
                Instant.now(), userId, tenantId, requestedPermission, endpoint, ipAddress,
                SecurityEventType.ACCESS_DENIED);
        repository.save(event);
        log.warn("Access denied: user={} tenant={} endpoint={} requiredPermission={} ip={}",
                userId, tenantId, endpoint, requestedPermission, ipAddress);
    }
}

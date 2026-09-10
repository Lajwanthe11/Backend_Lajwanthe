package com.example.rbac.service;

import com.example.rbac.entity.SecurityEvent;
import com.example.rbac.enums.SecurityEventType;
import com.example.rbac.repository.SecurityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

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

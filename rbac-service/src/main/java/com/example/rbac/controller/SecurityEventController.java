package com.example.rbac.controller;

import com.example.rbac.config.RequirePermission;
import com.example.rbac.entity.SecurityEvent;
import com.example.rbac.repository.SecurityEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /api/v1/security/events} - admin-only security event log.
 * Gated by {@code @RequirePermission} itself, so it also demonstrates the
 * guard protecting a "meta" security feature, not just business endpoints.
 */
@RestController
@RequestMapping("/api/v1/security")
public class SecurityEventController {

    private final SecurityEventRepository repository;

    public SecurityEventController(SecurityEventRepository repository) {
        this.repository = repository;
    }

    @RequirePermission("SECURITY_EVENTS_VIEW")
    @GetMapping("/events")
    public Page<SecurityEvent> getSecurityEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return repository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }
}

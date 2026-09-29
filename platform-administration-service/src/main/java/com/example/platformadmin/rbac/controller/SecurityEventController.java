package com.example.platformadmin.rbac.controller;

import com.example.platformadmin.rbac.config.RequirePermission;
import com.example.platformadmin.rbac.entity.SecurityEvent;
import com.example.platformadmin.rbac.repository.SecurityEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/security")
public class SecurityEventController {

    // Repository used to fetch security events from the database.
    private final SecurityEventRepository repository;

    // Injects the SecurityEventRepository dependency via constructor.
    public SecurityEventController(SecurityEventRepository repository) {
        this.repository = repository;
    }

    // GET /api/v1/security/events — returns paginated security events, newest
    // first. Requires SECURITY_EVENTS_VIEW permission.
    @RequirePermission("SECURITY_EVENTS_VIEW")
    @GetMapping("/events")
    public Page<SecurityEvent> getSecurityEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return repository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }
}

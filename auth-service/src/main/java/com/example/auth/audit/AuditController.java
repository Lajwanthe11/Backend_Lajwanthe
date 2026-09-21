package com.example.auth.audit;

import com.example.common.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping("/events")
    @Operation(
            summary = "View audit events",
            description = "Returns audit events for the current tenant"
    )
    public ResponseEntity<List<AuditEvent>> getAuditEvents() {

        String tenantId = TenantContext.getTenantId();

        List<AuditEvent> events =
                auditEventRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);

        return ResponseEntity.ok(events);
    }
}
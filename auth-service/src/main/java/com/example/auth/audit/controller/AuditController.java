package com.example.auth.audit.controller;

import com.example.auth.audit.dto.AuditRequestDto;
import com.example.auth.audit.dto.AuditResponseDto;
import com.example.auth.audit.entity.AuditEvent;
import com.example.auth.audit.service.AuditService;
import com.example.common.abstracts.AbstractController;
import com.example.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only view over the authentication audit trail.
 *
 * Extends AbstractController per team convention, matching AuditService
 * which extends AbstractService. The inherited update/deleteById endpoints
 * are overridden below to reject every call with 405 Method Not Allowed —
 * the audit trail must remain append-only. AuditService also throws
 * UnsupportedOperationException on update()/deleteById() as a second layer
 * of defense. Events are written in-process by {@link AuditService} only.
 *
 * Every endpoint here requires ROLE_ADMIN — an audit trail is not ordinary user data.
 */
@RestController
@RequestMapping("/audit")
@Tag(name = "Audit & Compliance", description = "Read-only access to the authentication audit trail (admin only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditController extends AbstractController<AuditEvent, Long, AuditRequestDto, AuditResponseDto> {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        super(auditService);
        this.auditService = auditService;
    }

    @GetMapping("/events")
    @Operation(
            summary = "View audit events",
            description = "Returns audit events for the current tenant. Requires ROLE_ADMIN."
    )
    public ResponseEntity<List<AuditResponseDto>> getAuditEvents() {
        return ResponseEntity.ok(auditService.getAll());
    }


    @Override
    @Operation(summary = "Not supported", description = "Audit events are append-only and cannot be updated.")
    public ResponseEntity<ApiResponse<AuditResponseDto>> update(@PathVariable Long id,@Valid
    @RequestBody AuditRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @Operation(summary = "Not supported", description = "Audit events are append-only and cannot be deleted.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}

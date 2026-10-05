package com.example.auth.audit.service;

import com.example.auth.audit.dto.AuditRequestDto;
import com.example.auth.audit.dto.AuditResponseDto;
import com.example.auth.audit.entity.AuditEvent;
import com.example.auth.audit.repository.AuditEventRepository;
import com.example.common.abstracts.AbstractService;
import org.springframework.stereotype.Service;

/**
 * Writes and reads the authentication audit trail.
 *
 * Extends AbstractService since AuditEvent extends BaseEntity, matching the
 * established pattern for entities in that hierarchy. update() and
 * deleteById() are overridden to reject mutation — the audit trail must
 * remain append-only. Records are created in-process by AuthService (login,
 * register, token refresh, lockout) and are never modified or removed
 * afterwards.
 */
@Service
public class AuditService extends AbstractService<AuditEvent, Long, AuditRequestDto, AuditResponseDto> {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        super(auditEventRepository, "Audit event");
        this.auditEventRepository = auditEventRepository;
    }


    @Override
    protected AuditEvent toEntity(AuditRequestDto dto) {
        AuditEvent event = new AuditEvent();
        event.setEventType(dto.getEventType());
        event.setModule(dto.getModule());
        event.setStatus(dto.getStatus());
        event.setUsername(dto.getUsername());
        event.setDetails(dto.getDetails());
        return event;
    }

    @Override
    protected AuditResponseDto toDto(AuditEvent event) {
        AuditResponseDto response = new AuditResponseDto();
        response.setId(event.getId());                    // inherited from BaseEntity
        response.setEventType(event.getEventType());
        response.setModule(event.getModule());
        response.setStatus(event.getStatus());
        response.setUsername(event.getUsername());
        response.setDetails(event.getDetails());
        response.setTenantId(event.getTenantId());          // inherited from BaseEntity
        response.setCreatedAt(event.getCreatedAt());        // inherited from BaseEntity
        return response;
    }

    @Override
    protected void updateEntityFromDto(AuditEvent event, AuditRequestDto dto) {
        throw new UnsupportedOperationException(
                "Audit events are append-only and cannot be updated.");
    }



    @Override
    public AuditResponseDto update(Long id, AuditRequestDto requestDto) {
        throw new UnsupportedOperationException(
                "Audit events are append-only and cannot be updated.");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException(
                "Audit events are append-only and cannot be deleted.");
    }



    public void loginSuccess(String username, String tenantId) {
        saveAuditEvent("LOGIN_SUCCESS", username, tenantId, "SUCCESS", "User logged in successfully");
    }

    public void loginFailed(String username, String tenantId) {
        saveAuditEvent("LOGIN_FAILED", username, tenantId, "FAILED", "Invalid login credentials");
    }

    public void accountLocked(String username, String tenantId) {
        saveAuditEvent("ACCOUNT_LOCKED", username, tenantId, "SUCCESS", "Account locked after multiple failed login attempts");
    }

    public void userRegistered(String username, String tenantId) {
        saveAuditEvent("USER_REGISTERED", username, tenantId, "SUCCESS", "User registered successfully");
    }

    public void tokenRefresh(String username, String tenantId) {
        saveAuditEvent("TOKEN_REFRESH", username, tenantId, "SUCCESS", "Access token refreshed successfully");
    }

    public void passwordReset(String username, String tenantId) {
        AuditEvent event = new AuditEvent();
        event.setEventType("PASSWORD_RESET");
        event.setModule("AUTH");
        event.setStatus("SUCCESS");
        event.setUsername(username);
        event.setDetails("Password reset completed");
        auditEventRepository.save(event);
    }

    private void saveAuditEvent(String eventType, String username, String tenantId, String status, String details) {
        AuditRequestDto requestDto = new AuditRequestDto();
        requestDto.setEventType(eventType);
        requestDto.setModule("AUTH");
        requestDto.setStatus(status);
        requestDto.setUsername(username);
        requestDto.setDetails("Tenant: " + tenantId + " | " + details);
        create(requestDto);
    }
}
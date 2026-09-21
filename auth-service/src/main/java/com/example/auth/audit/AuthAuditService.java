package com.example.auth.audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service

public class AuthAuditService {
   private static final Logger log=LoggerFactory.getLogger(AuthAuditService.class);
    private final AuditEventRepository auditEventRepository;

    public AuthAuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void loginSuccess(String username, String tenantId) {

        saveAuditEvent(
                "LOGIN_SUCCESS",
                username,
                tenantId,
                "SUCCESS",
                "User login successful"
        );
    }

    public void loginFailed(String username, String tenantId) {

        saveAuditEvent(
                "LOGIN_FAILED",
                username,
                tenantId,
                "FAILURE",
                "Invalid login credentials"
        );
    }

    public void accountLocked(String username, String tenantId) {

        saveAuditEvent(
                "ACCOUNT_LOCKED",
                username,
                tenantId,
                "FAILURE",
                "User account locked after multiple failed login attempts"
        );
    }

    public void userRegistered(String username, String tenantId) {

        saveAuditEvent(
                "USER_REGISTERED",
                username,
                tenantId,
                "SUCCESS",
                "User account registered"
        );
    }

    public void tokenRefresh(String username, String tenantId) {

        saveAuditEvent(
                "TOKEN_REFRESH",
                username,
                tenantId,
                "SUCCESS",
                "Access token refreshed"
        );
    }

    public void passwordReset(String username, String tenantId) {

        saveAuditEvent(
                "PASSWORD_RESET",
                username,
                tenantId,
                "SUCCESS",
                "Password reset completed"
        );
    }

    private void saveAuditEvent(
            String eventType,
            String username,
            String tenantId,
            String status,
            String details) {

        AuditEvent auditEvent = new AuditEvent();

        auditEvent.setEventType(eventType);
        auditEvent.setModule("AUTH");
        auditEvent.setStatus(status);
        auditEvent.setUsername(username);
        auditEvent.setDetails(details);


        auditEvent.setTenantId(tenantId);

        auditEventRepository.save(auditEvent);

        log.info(
                "AUDIT event={} username={} tenantId={} module=AUTH status={}",
                eventType,
                username,
                tenantId,
                status
        );
    }
}
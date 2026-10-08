package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.entity.UserRole;
import com.example.platformadmin.rbac.service.RoleExpiryNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingRoleExpiryNotificationServiceImpl implements RoleExpiryNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingRoleExpiryNotificationServiceImpl.class);

    @Override
    public void notifyUserAndAdmin(UserRole assignment, String roleCode, String roleName) {
        // Replace this logging implementation with the team's notification-service call
        // when the shared service contract is available.
        log.info(
                "ROLE_EXPIRY_NOTIFICATION userId={} tenantId={} roleId={} roleCode={} roleName={} expiryDate={} recipients=USER,ADMIN",
                assignment.getUserId(),
                assignment.getTenantId(),
                assignment.getRoleId(),
                roleCode,
                roleName,
                assignment.getExpiryDate()
        );
    }
}

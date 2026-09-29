package com.example.platformadmin.rbac.scheduler;

import com.example.platformadmin.rbac.entity.Role;

import com.example.platformadmin.rbac.entity.RoleAuditLog;
import com.example.platformadmin.rbac.entity.UserRole;
import com.example.platformadmin.rbac.repository.RoleAuditLogRepository;
import com.example.platformadmin.rbac.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleExpiryScheduler {

    private final UserRoleRepository userRoleRepository;

    private final RoleAuditLogRepository auditLogRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireRoles() {

        LocalDate today = LocalDate.now();

        List<UserRole> expiredRoles =
                userRoleRepository.findExpiredRoles(today);

        for (UserRole userRole : expiredRoles) {

            userRole.setActive(false);

            userRole.setPrimary(false);

            userRole.setRevokedAt(LocalDateTime.now());

            userRole.setRevokeReason("Role expired automatically");

            userRoleRepository.save(userRole);

            RoleAuditLog auditLog =
                    RoleAuditLog.builder()
                            .userRoleId(
                                    userRole.getUserRoleId()
                            )
                            .userId(userRole.getUserId())
                            .roleId(userRole.getRoleId())
                            .tenantId(userRole.getTenantId())
                            .action("ROLE_EXPIRED")
                            .performedAt(
                                    LocalDateTime.now()
                            )
                            .reason(
                                    "Role expired automatically"
                            )
                            .build();

            auditLogRepository.save(auditLog);

            log.info(
                    "Role {} expired for user {}",
                    userRole.getRoleId(),
                    userRole.getUserId()
            );
        }
    }
}
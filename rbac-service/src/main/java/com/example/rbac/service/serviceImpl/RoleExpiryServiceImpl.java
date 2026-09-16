package com.example.rbac.service.impl;

import com.example.rbac.service.RoleExpiryNotificationService;
import com.example.rbac.dto.ExpiryNotificationResponse;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.UserRole;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRoleRepository;
import com.example.rbac.service.RoleExpiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RoleExpiryServiceImpl implements RoleExpiryService {

    private static final Logger log = LoggerFactory.getLogger(RoleExpiryServiceImpl.class);
    private static final int NOTIFICATION_DAYS_BEFORE_EXPIRY = 7;

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RoleExpiryNotificationService notificationService;

    public RoleExpiryServiceImpl(
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            RoleExpiryNotificationService notificationService
    ) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ExpiryNotificationResponse triggerForTenant(UUID tenantId) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(NOTIFICATION_DAYS_BEFORE_EXPIRY);
        List<UserRole> assignments = userRoleRepository.findTenantAssignmentsExpiringOn(
                tenantId, today, targetDate);
        int sent = notifyAssignments(assignments);
        return new ExpiryNotificationResponse(targetDate, assignments.size(), sent);
    }

    @Scheduled(
            cron = "${rbac.expiry.notification-cron:0 0 0 * * *}",
            zone = "${rbac.expiry.notification-zone:UTC}"
    )
    public void scheduledExpiryNotifications() {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(NOTIFICATION_DAYS_BEFORE_EXPIRY);
        List<UserRole> assignments =
                userRoleRepository.findAllAssignmentsExpiringOn(today, targetDate);
        int sent = notifyAssignments(assignments);
        log.info(
                "Completed role expiry notification job targetDate={} matched={} sent={}",
                targetDate, assignments.size(), sent
        );
    }

    private int notifyAssignments(List<UserRole> assignments) {
        int sent = 0;
        for (UserRole assignment : assignments) {
            try {
                Role role = roleRepository.findById(assignment.getRoleId()).orElse(null);
                notificationService.notifyUserAndAdmin(
                        assignment,
                        role == null ? null : role.getRoleCode(),
                        role == null ? null : role.getRoleName()
                );
                sent++;
            } catch (RuntimeException ex) {
                log.error(
                        "Failed to trigger expiry notification for userRoleId={}",
                        assignment.getUserRoleId(), ex
                );
            }
        }
        return sent;
    }
}

package com.example.rbac.service;

import com.example.rbac.service.serviceImpl.RoleExpiryServiceImpl;

import com.example.rbac.dto.ExpiryNotificationResponse;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.UserRole;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRoleRepository;
import com.example.rbac.service.RoleExpiryNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleExpiryServiceImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RoleExpiryNotificationService notificationService;

    private RoleExpiryServiceImpl service;
    private UUID tenantId;
    private UUID roleId;
    private Role role;

    @BeforeEach
    void setUp() {
        service = new RoleExpiryServiceImpl(
                userRoleRepository, roleRepository, notificationService);
        tenantId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        role = new Role();
        role.setRoleId(roleId);
        role.setTenantId(tenantId);
        role.setRoleCode("HR_MANAGER");
        role.setRoleName("HR Manager");
    }

    @Test
    void triggerForTenant_shouldNotifyAssignmentsExpiringInSevenDays() {
        LocalDate today = LocalDate.now();
        UserRole first = assignment(UUID.randomUUID(), today.plusDays(7));
        UserRole second = assignment(UUID.randomUUID(), today.plusDays(7));

        when(userRoleRepository.findTenantAssignmentsExpiringOn(
                eq(tenantId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(first, second));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        ExpiryNotificationResponse response = service.triggerForTenant(tenantId);

        assertEquals(today.plusDays(7), response.targetExpiryDate());
        assertEquals(2, response.matchedAssignments());
        assertEquals(2, response.notificationsTriggered());
        verify(notificationService, times(2))
                .notifyUserAndAdmin(any(UserRole.class), eq("HR_MANAGER"), eq("HR Manager"));
    }

    @Test
    void triggerForTenant_shouldContinueIfOneNotificationFails() {
        LocalDate target = LocalDate.now().plusDays(7);
        UserRole first = assignment(UUID.randomUUID(), target);
        UserRole second = assignment(UUID.randomUUID(), target);

        when(userRoleRepository.findTenantAssignmentsExpiringOn(
                eq(tenantId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(first, second));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        doThrow(new RuntimeException("Mail service down"))
                .doNothing()
                .when(notificationService)
                .notifyUserAndAdmin(any(UserRole.class), eq("HR_MANAGER"), eq("HR Manager"));

        ExpiryNotificationResponse response = service.triggerForTenant(tenantId);

        assertEquals(2, response.matchedAssignments());
        assertEquals(1, response.notificationsTriggered());
    }

    @Test
    void scheduledExpiryNotifications_shouldUseAllTenantQuery() {
        UserRole assignment = assignment(UUID.randomUUID(), LocalDate.now().plusDays(7));
        when(userRoleRepository.findAllAssignmentsExpiringOn(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(assignment));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        service.scheduledExpiryNotifications();

        verify(userRoleRepository).findAllAssignmentsExpiringOn(
                eq(LocalDate.now()), eq(LocalDate.now().plusDays(7)));
        verify(notificationService).notifyUserAndAdmin(
                assignment, "HR_MANAGER", "HR Manager");
    }

    @Test
    void triggerForTenant_shouldStillNotifyWhenRoleRecordIsMissing() {
        UserRole assignment = assignment(UUID.randomUUID(), LocalDate.now().plusDays(7));
        when(userRoleRepository.findTenantAssignmentsExpiringOn(
                eq(tenantId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(assignment));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        ExpiryNotificationResponse response = service.triggerForTenant(tenantId);

        assertEquals(1, response.notificationsTriggered());
        verify(notificationService).notifyUserAndAdmin(assignment, null, null);
    }

    private UserRole assignment(UUID userId, LocalDate expiryDate) {
        return UserRole.builder()
                .userRoleId(UUID.randomUUID())
                .tenantId(tenantId)
                .userId(userId)
                .roleId(roleId)
                .effectiveDate(LocalDate.now().minusDays(1))
                .expiryDate(expiryDate)
                .assignedBy(UUID.randomUUID())
                .assignedAt(LocalDateTime.now())
                .active(true)
                .build();
    }
}

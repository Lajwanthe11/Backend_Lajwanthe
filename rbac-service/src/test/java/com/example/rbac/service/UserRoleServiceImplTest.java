package com.example.rbac.service;

import com.example.rbac.service.CurrentUserService;
import com.example.rbac.config.SessionService;
import com.example.rbac.dto.request.AssignRoleRequest;
import com.example.rbac.dto.request.RevokeRoleRequest;
import com.example.rbac.dto.request.RoleAssignmentRequest;
import com.example.rbac.dto.response.UserRoleResponse;
import com.example.rbac.entity.UserRole;
import com.example.rbac.exception.InvalidRoleAssignmentException;
import com.example.rbac.exception.LastRoleRemovalException;
import com.example.rbac.exception.UserRoleNotFoundException;
import com.example.rbac.repository.RoleAuditLogRepository;
import com.example.rbac.repository.UserRoleRepository;
import com.example.rbac.service.serviceImpl.UserRoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserRoleServiceImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleAuditLogRepository auditLogRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    private UUID userId;
    private UUID roleId;
    private UUID tenantId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        userId   = UUID.randomUUID();
        roleId   = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        adminId  = UUID.randomUUID();

        when(currentUserService.getTenantId()).thenReturn(tenantId);
        when(currentUserService.getUserId()).thenReturn(adminId);
        when(currentUserService.isSuperAdmin()).thenReturn(false);
    }

    // ------------------------------------------------------------------ helpers

    private UserRole activeRole(UUID uid, UUID rid, UUID tid, boolean primary) {
        return UserRole.builder()
                .userRoleId(UUID.randomUUID())
                .userId(uid)
                .roleId(rid)
                .tenantId(tid)
                .primary(primary)
                .effectiveDate(LocalDate.now().minusDays(1))
                .assignedBy(adminId)
                .assignedAt(LocalDateTime.now())
                .active(true)
                .build();
    }

    private RoleAssignmentRequest roleRequest(UUID rid, boolean primary, LocalDate effective, LocalDate expiry) {
        RoleAssignmentRequest r = new RoleAssignmentRequest();
        r.setRoleId(rid);
        r.setPrimary(primary);
        r.setEffectiveDate(effective);
        r.setExpiryDate(expiry);
        return r;
    }

    private AssignRoleRequest assignRequest(RoleAssignmentRequest... requests) {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setRoles(List.of(requests));
        return req;
    }

    // ------------------------------------------------------------------ assignRoles

    @Test
    void assignRoles_success_savesRoleAndAuditLog() {
        RoleAssignmentRequest roleReq = roleRequest(roleId, false, LocalDate.now(), null);
        AssignRoleRequest request = assignRequest(roleReq);

        when(userRoleRepository.findByUserIdAndTenantIdAndActiveTrue(userId, tenantId))
                .thenReturn(List.of());
        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.empty());

        UserRole saved = activeRole(userId, roleId, tenantId, false);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(saved);

        List<UserRoleResponse> result = userRoleService.assignRoles(userId, request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleId()).isEqualTo(roleId);
        verify(userRoleRepository, times(1)).save(any(UserRole.class));
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void assignRoles_throwsInvalidRoleAssignment_whenMoreThanOnePrimaryInRequest() {
        UUID roleId2 = UUID.randomUUID();
        AssignRoleRequest request = assignRequest(
                roleRequest(roleId,  true, LocalDate.now(), null),
                roleRequest(roleId2, true, LocalDate.now(), null)
        );

        assertThatThrownBy(() -> userRoleService.assignRoles(userId, request))
                .isInstanceOf(InvalidRoleAssignmentException.class)
                .hasMessageContaining("Only one primary role");
    }

    @Test
    void assignRoles_throwsInvalidRoleAssignment_whenDuplicateActiveRoleExists() {
        RoleAssignmentRequest roleReq = roleRequest(roleId, false, LocalDate.now(), null);
        AssignRoleRequest request = assignRequest(roleReq);

        when(userRoleRepository.findByUserIdAndTenantIdAndActiveTrue(userId, tenantId))
                .thenReturn(List.of());
        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.of(activeRole(userId, roleId, tenantId, false)));

        assertThatThrownBy(() -> userRoleService.assignRoles(userId, request))
                .isInstanceOf(InvalidRoleAssignmentException.class)
                .hasMessageContaining("already assigned");
    }

    @Test
    void assignRoles_throwsInvalidRoleAssignment_whenExpiryNotAfterEffectiveDate() {
        LocalDate effective = LocalDate.now();
        LocalDate expiry    = LocalDate.now(); // same day — not after

        RoleAssignmentRequest roleReq = roleRequest(roleId, false, effective, expiry);
        AssignRoleRequest request = assignRequest(roleReq);

        when(userRoleRepository.findByUserIdAndTenantIdAndActiveTrue(userId, tenantId))
                .thenReturn(List.of());
        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userRoleService.assignRoles(userId, request))
                .isInstanceOf(InvalidRoleAssignmentException.class)
                .hasMessageContaining("Expiry date must be after effective date");
    }

    @Test
    void assignRoles_demotesExistingPrimary_whenNewPrimaryAssigned() {
        UserRole existingPrimary = activeRole(userId, UUID.randomUUID(), tenantId, true);

        RoleAssignmentRequest roleReq = roleRequest(roleId, true, LocalDate.now(), null);
        AssignRoleRequest request = assignRequest(roleReq);

        when(userRoleRepository.findByUserIdAndTenantIdAndActiveTrue(userId, tenantId))
                .thenReturn(List.of(existingPrimary));
        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.empty());
        when(userRoleRepository.findByUserIdAndTenantIdAndPrimaryTrueAndActiveTrue(userId, tenantId))
                .thenReturn(Optional.of(existingPrimary));

        UserRole saved = activeRole(userId, roleId, tenantId, true);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(saved);

        userRoleService.assignRoles(userId, request);

        // save called once for demoting old primary + once for new role
        verify(userRoleRepository, atLeast(2)).save(any(UserRole.class));
        assertThat(existingPrimary.isPrimary()).isFalse();
    }

    // ------------------------------------------------------------------ getCurrentRoles

    @Test
    void getCurrentRoles_returnsMappedResponses() {
        UserRole role = activeRole(userId, roleId, tenantId, false);
        when(userRoleRepository.findCurrentRoles(userId, tenantId, LocalDate.now()))
                .thenReturn(List.of(role));

        List<UserRoleResponse> result = userRoleService.getCurrentRoles(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
    }

    // ------------------------------------------------------------------ revokeRole

    @Test
    void revokeRole_success_deactivatesRoleAndLogsAudit() {
        UserRole role  = activeRole(userId, roleId, tenantId, false);
        UserRole role2 = activeRole(userId, UUID.randomUUID(), tenantId, false);

        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.of(role));
        when(userRoleRepository.findCurrentRoles(userId, tenantId, LocalDate.now()))
                .thenReturn(List.of(role, role2));
        when(userRoleRepository.save(any())).thenReturn(role);

        RevokeRoleRequest req = new RevokeRoleRequest();
        req.setReason("No longer needed");

        userRoleService.revokeRole(userId, roleId, req);

        assertThat(role.isActive()).isFalse();
        assertThat(role.getRevokeReason()).isEqualTo("No longer needed");
        verify(auditLogRepository).save(any());
    }

    @Test
    void revokeRole_throwsUserRoleNotFound_whenNoActiveAssignment() {
        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.empty());

        RevokeRoleRequest req = new RevokeRoleRequest();
        req.setReason("reason");

        assertThatThrownBy(() -> userRoleService.revokeRole(userId, roleId, req))
                .isInstanceOf(UserRoleNotFoundException.class)
                .hasMessageContaining("Active role assignment not found");
    }

    @Test
    void revokeRole_throwsLastRoleRemoval_whenOnlyOneCurrentRole() {
        UserRole role = activeRole(userId, roleId, tenantId, false);

        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.of(role));
        when(userRoleRepository.findCurrentRoles(userId, tenantId, LocalDate.now()))
                .thenReturn(List.of(role));

        RevokeRoleRequest req = new RevokeRoleRequest();
        req.setReason("reason");

        assertThatThrownBy(() -> userRoleService.revokeRole(userId, roleId, req))
                .isInstanceOf(LastRoleRemovalException.class)
                .hasMessageContaining("last role");
    }

    // ------------------------------------------------------------------ setPrimaryRole

    @Test
    void setPrimaryRole_success_promotesNewAndDemotesOld() {
        UserRole newPrimary = activeRole(userId, roleId, tenantId, false);
        UserRole oldPrimary = activeRole(userId, UUID.randomUUID(), tenantId, true);

        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.of(newPrimary));
        when(userRoleRepository.findByUserIdAndTenantIdAndPrimaryTrueAndActiveTrue(userId, tenantId))
                .thenReturn(Optional.of(oldPrimary));
        when(userRoleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userRoleService.setPrimaryRole(userId, roleId);

        assertThat(newPrimary.isPrimary()).isTrue();
        assertThat(oldPrimary.isPrimary()).isFalse();
        verify(sessionService).invalidateUserSessions(userId);
        verify(auditLogRepository).save(any());
    }

    @Test
    void setPrimaryRole_throwsUserRoleNotFound_whenRoleNotFound() {
        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userRoleService.setPrimaryRole(userId, roleId))
                .isInstanceOf(UserRoleNotFoundException.class)
                .hasMessageContaining("Role assignment not found");
    }

    @Test
    void setPrimaryRole_throwsInvalidRoleAssignment_whenEffectiveDateIsInFuture() {
        UserRole futureRole = UserRole.builder()
                .userRoleId(UUID.randomUUID())
                .userId(userId)
                .roleId(roleId)
                .tenantId(tenantId)
                .effectiveDate(LocalDate.now().plusDays(5))
                .active(true)
                .build();

        when(userRoleRepository.findByUserIdAndRoleIdAndTenantIdAndActiveTrue(userId, roleId, tenantId))
                .thenReturn(Optional.of(futureRole));

        assertThatThrownBy(() -> userRoleService.setPrimaryRole(userId, roleId))
                .isInstanceOf(InvalidRoleAssignmentException.class)
                .hasMessageContaining("Future effective role cannot be primary yet");
    }

    // ------------------------------------------------------------------ getRoleHistory

    @Test
    void getRoleHistory_returnsMappedResponsesOrderedByAssignedAt() {
        UserRole r1 = activeRole(userId, roleId, tenantId, false);
        UserRole r2 = activeRole(userId, UUID.randomUUID(), tenantId, false);

        when(userRoleRepository.findByUserIdAndTenantIdOrderByAssignedAtDesc(userId, tenantId))
                .thenReturn(List.of(r1, r2));

        List<UserRoleResponse> result = userRoleService.getRoleHistory(userId);

        assertThat(result).hasSize(2);
        verify(userRoleRepository).findByUserIdAndTenantIdOrderByAssignedAtDesc(userId, tenantId);
    }

    // ------------------------------------------------------------------ getUsersByRole

    @Test
    void getUsersByRole_returnsMappedActiveAssignments() {
        UserRole role = activeRole(userId, roleId, tenantId, false);

        when(userRoleRepository.findByRoleIdAndTenantIdAndActiveTrue(roleId, tenantId))
                .thenReturn(List.of(role));

        List<UserRoleResponse> result = userRoleService.getUsersByRole(roleId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleId()).isEqualTo(roleId);
    }
}

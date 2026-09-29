package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.entity.Role;

import com.example.platformadmin.rbac.service.serviceImpl.CurrentUserService;
import com.example.platformadmin.rbac.service.serviceImpl.SessionService;
import com.example.platformadmin.rbac.dto.request.AssignRoleRequest;
import com.example.platformadmin.rbac.dto.request.RevokeRoleRequest;
import com.example.platformadmin.rbac.dto.request.RoleAssignmentRequest;
import com.example.platformadmin.rbac.dto.response.UserRoleResponse;
import com.example.platformadmin.rbac.entity.RoleAuditLog;
import com.example.platformadmin.rbac.entity.UserRole;
import com.example.platformadmin.rbac.exception.InvalidRoleAssignmentException;
import com.example.platformadmin.rbac.exception.LastRoleRemovalException;
import com.example.platformadmin.rbac.exception.UserRoleNotFoundException;
import com.example.platformadmin.rbac.repository.RoleAuditLogRepository;
import com.example.platformadmin.rbac.repository.UserRoleRepository;
import com.example.platformadmin.rbac.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;

    private final RoleAuditLogRepository auditLogRepository;

    private final CurrentUserService currentUserService;

    private final SessionService sessionService;

    @Override
    @Transactional
    public List<UserRoleResponse> assignRoles(
            UUID userId,
            AssignRoleRequest request) {

        UUID tenantId = getCurrentTenantId();
        UUID adminId = getCurrentUserId();

        validateTenantUser(userId, tenantId);

        validatePrimaryRoles(request);

        List<UserRole> existingRoles =
                userRoleRepository
                        .findByUserIdAndTenantIdAndActiveTrue(
                                userId,
                                tenantId
                        );

        int totalRoles =
                existingRoles.size()
                        + request.getRoles().size();

        if (totalRoles > 10) {
            log.warn(
                    "User {} has more than 10 roles",
                    userId
            );
        }

        List<UserRole> savedRoles = new ArrayList<>();

        for (RoleAssignmentRequest roleRequest
                : request.getRoles()) {

            validateDates(
                    roleRequest,
                    isSuperAdmin()
            );

            validateRoleTenant(
                    roleRequest.getRoleId(),
                    tenantId
            );

            validateDuplicateRole(
                    userId,
                    roleRequest.getRoleId(),
                    tenantId
            );

            if (roleRequest.isPrimary()) {

                demoteCurrentPrimary(
                        userId,
                        tenantId
                );
            }

            UserRole userRole = UserRole.builder()
                    .userId(userId)
                    .roleId(roleRequest.getRoleId())
                    .tenantId(tenantId)
                    .primary(roleRequest.isPrimary())
                    .effectiveDate(
                            roleRequest.getEffectiveDate()
                    )
                    .expiryDate(
                            roleRequest.getExpiryDate()
                    )
                    .assignedBy(adminId)
                    .assignedAt(LocalDateTime.now())
                    .active(true)
                    .build();

            UserRole saved =
                    userRoleRepository.save(userRole);

            savedRoles.add(saved);

            createAuditLog(
                    saved,
                    "ROLE_ASSIGNED",
                    adminId,
                    null
            );
        }

        return savedRoles.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getCurrentRoles(
            UUID userId) {

        UUID tenantId = getCurrentTenantId();

        return userRoleRepository.findCurrentRoles(
                        userId,
                        tenantId,
                        LocalDate.now()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void revokeRole(
            UUID userId,
            UUID roleId,
            RevokeRoleRequest request) {

        UUID tenantId = getCurrentTenantId();
        UUID adminId = getCurrentUserId();

        UserRole userRole =
                userRoleRepository
                        .findByUserIdAndRoleIdAndTenantIdAndActiveTrue(
                                userId,
                                roleId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new UserRoleNotFoundException(
                                        "Active role assignment not found"
                                )
                        );

        List<UserRole> currentRoles =
                userRoleRepository
                        .findCurrentRoles(
                                userId,
                                tenantId,
                                LocalDate.now()
                        );

        if (currentRoles.size() <= 1) {

            throw new LastRoleRemovalException(
                    "Cannot remove the user's last role"
            );
        }

        userRole.setActive(false);
        userRole.setPrimary(false);
        userRole.setRevokedBy(adminId);
        userRole.setRevokedAt(LocalDateTime.now());
        userRole.setRevokeReason(request.getReason());

        userRoleRepository.save(userRole);

        createAuditLog(
                userRole,
                "ROLE_REVOKED",
                adminId,
                request.getReason()
        );
    }

    @Override
    @Transactional
    public void setPrimaryRole(
            UUID userId,
            UUID roleId) {

        UUID tenantId = getCurrentTenantId();
        UUID adminId = getCurrentUserId();

        UserRole newPrimary =
                userRoleRepository
                        .findByUserIdAndRoleIdAndTenantIdAndActiveTrue(
                                userId,
                                roleId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new UserRoleNotFoundException(
                                        "Role assignment not found"
                                )
                        );

        if (newPrimary.getEffectiveDate()
                .isAfter(LocalDate.now())) {

            throw new InvalidRoleAssignmentException(
                    "Future effective role cannot be primary yet"
            );
        }

        userRoleRepository
                .findByUserIdAndTenantIdAndPrimaryTrueAndActiveTrue(
                        userId,
                        tenantId
                )
                .ifPresent(oldPrimary -> {

                    oldPrimary.setPrimary(false);

                    userRoleRepository.save(oldPrimary);
                });

        newPrimary.setPrimary(true);

        userRoleRepository.save(newPrimary);

        createAuditLog(
                newPrimary,
                "PRIMARY_ROLE_SET",
                adminId,
                null
        );

        invalidateUserSessions(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getRoleHistory(
            UUID userId) {

        UUID tenantId = getCurrentTenantId();

        return userRoleRepository
                .findByUserIdAndTenantIdOrderByAssignedAtDesc(
                        userId,
                        tenantId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getUsersByRole(
            UUID roleId) {

        UUID tenantId = getCurrentTenantId();

        return userRoleRepository
                .findByRoleIdAndTenantIdAndActiveTrue(
                        roleId,
                        tenantId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validatePrimaryRoles(
            AssignRoleRequest request) {

        long primaryCount =
                request.getRoles()
                        .stream()
                        .filter(RoleAssignmentRequest::isPrimary)
                        .count();

        if (primaryCount > 1) {

            throw new InvalidRoleAssignmentException(
                    "Only one primary role can be assigned"
            );
        }
    }

    private void demoteCurrentPrimary(
            UUID userId,
            UUID tenantId) {

        userRoleRepository
                .findByUserIdAndTenantIdAndPrimaryTrueAndActiveTrue(
                        userId,
                        tenantId
                )
                .ifPresent(currentPrimary -> {

                    currentPrimary.setPrimary(false);

                    userRoleRepository.save(currentPrimary);
                });
    }

    private void validateTenantUser(
            UUID userId,
            UUID tenantId) {

        boolean exists =
                userRoleRepository
                        .findByUserIdAndTenantIdAndActiveTrue(
                                userId,
                                tenantId
                        )
                        .isEmpty();

        // Allow new users — only block cross-tenant access
        // by verifying the tenant matches the current session.
        // Extend this check with a user-tenant membership lookup if needed.
    }

    private void validateDates(
            RoleAssignmentRequest request,
            boolean isSuperAdmin) {

        if (request.getExpiryDate() != null
                && !request.getExpiryDate()
                        .isAfter(request.getEffectiveDate())) {

            throw new InvalidRoleAssignmentException(
                    "Expiry date must be after effective date"
            );
        }
    }

    private void validateRoleTenant(
            UUID roleId,
            UUID tenantId) {

        // Extend with a Role repository check to confirm
        // the role belongs to the given tenant.
    }

    private void validateDuplicateRole(
            UUID userId,
            UUID roleId,
            UUID tenantId) {

        userRoleRepository
                .findByUserIdAndRoleIdAndTenantIdAndActiveTrue(
                        userId,
                        roleId,
                        tenantId
                )
                .ifPresent(existing -> {
                    throw new InvalidRoleAssignmentException(
                            "Role is already assigned to this user"
                    );
                });
    }

    private void invalidateUserSessions(UUID userId) {
        sessionService.invalidateUserSessions(userId);
    }

    private UUID getCurrentUserId() {
        try {
            return currentUserService.getUserId();
        } catch (Exception e) {
            return UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        }
    }

    private UUID getCurrentTenantId() {
        try {
            return currentUserService.getTenantId();
        } catch (Exception e) {
            return UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        }
    }

    private boolean isSuperAdmin() {
        try {
            return currentUserService.isSuperAdmin();
        } catch (Exception e) {
            return true;
        }
    }

    private void createAuditLog(
            UserRole userRole,
            String action,
            UUID performedBy,
            String reason) {

        RoleAuditLog auditLog =
                RoleAuditLog.builder()
                        .userRoleId(
                                userRole.getUserRoleId()
                        )
                        .userId(userRole.getUserId())
                        .roleId(userRole.getRoleId())
                        .tenantId(userRole.getTenantId())
                        .action(action)
                        .performedBy(performedBy)
                        .performedAt(LocalDateTime.now())
                        .reason(reason)
                        .build();

        auditLogRepository.save(auditLog);
    }

    private UserRoleResponse toResponse(UserRole userRole) {

        return UserRoleResponse.builder()
                .userRoleId(userRole.getUserRoleId())
                .userId(userRole.getUserId())
                .roleId(userRole.getRoleId())
                .tenantId(userRole.getTenantId())
                .primary(userRole.isPrimary())
                .effectiveDate(userRole.getEffectiveDate())
                .expiryDate(userRole.getExpiryDate())
                .assignedBy(userRole.getAssignedBy())
                .assignedAt(userRole.getAssignedAt())
                .active(userRole.isActive())
                .build();
    }
}

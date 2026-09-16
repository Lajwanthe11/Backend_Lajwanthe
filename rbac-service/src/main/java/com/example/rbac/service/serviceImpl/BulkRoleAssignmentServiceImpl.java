package com.example.rbac.service.impl;

import com.example.rbac.service.RoleAuditService;
import com.example.rbac.service.UserAssignmentSupportService;
import com.example.rbac.dto.BulkOperationItemResult;
import com.example.rbac.dto.BulkOperationResponse;
import com.example.rbac.dto.BulkRoleAssignmentRequest;
import com.example.rbac.dto.BulkRoleRevokeRequest;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.UserRole;
import com.example.rbac.exception.RoleAssignmentValidationException;
import com.example.rbac.repository.UserRoleRepository;
import com.example.rbac.service.BulkRoleAssignmentService;
import com.example.rbac.service.RoleLookupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BulkRoleAssignmentServiceImpl implements BulkRoleAssignmentService {

    private static final int MAX_BULK_SIZE = 500;

    private final UserRoleRepository userRoleRepository;
    private final RoleLookupService roleLookupService;
    private final RoleAuditService roleAuditService;
    private final UserAssignmentSupportService userAssignmentSupportService;
    private final boolean atomicMode;

    public BulkRoleAssignmentServiceImpl(
            UserRoleRepository userRoleRepository,
            RoleLookupService roleLookupService,
            RoleAuditService roleAuditService,
            UserAssignmentSupportService userAssignmentSupportService,
            @Value("${rbac.bulk.atomic:true}") boolean atomicMode
    ) {
        this.userRoleRepository = userRoleRepository;
        this.roleLookupService = roleLookupService;
        this.roleAuditService = roleAuditService;
        this.userAssignmentSupportService = userAssignmentSupportService;
        this.atomicMode = atomicMode;
    }

    @Override
    @Transactional
    public BulkOperationResponse bulkAssign(
            UUID tenantId,
            UUID actorId,
            BulkRoleAssignmentRequest request
    ) {
        validateContext(tenantId, actorId);
        validateAssignmentRequest(request);

        Role role = roleLookupService.getAssignableRole(tenantId, request.roleId());
        List<BulkOperationItemResult> results = new ArrayList<>();
        List<UserRole> stagedAssignments = new ArrayList<>();

        LinkedHashSet<UUID> uniqueUsers = new LinkedHashSet<>();
        for (UUID userId : request.userIds()) {
            if (!uniqueUsers.add(userId)) {
                results.add(new BulkOperationItemResult(
                        userId, "SKIPPED", "Duplicate userId in request"));
            }
        }

        for (UUID userId : uniqueUsers) {
            try {
                userAssignmentSupportService.validateAssignable(tenantId, userId);

                if (userRoleRepository.existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId, userId, request.roleId())) {
                    results.add(new BulkOperationItemResult(
                            userId, "SKIPPED", "already assigned"));
                    continue;
                }

                UserRole assignment = new UserRole();
                assignment.setTenantId(tenantId);
                assignment.setUserId(userId);
                assignment.setRoleId(request.roleId());
                assignment.setPrimary(false);
                assignment.setEffectiveDate(request.effectiveDate());
                assignment.setExpiryDate(request.expiryDate());
                assignment.setAssignedBy(actorId);
                assignment.setAssignedAt(LocalDateTime.now());
                assignment.setActive(true);
                stagedAssignments.add(assignment);
            } catch (RuntimeException ex) {
                if (atomicMode) {
                    throw ex;
                }
                results.add(new BulkOperationItemResult(userId, "FAILED", safeMessage(ex)));
            }
        }

        List<UserRole> saved = userRoleRepository.saveAll(stagedAssignments);
        for (UserRole assignment : saved) {
            roleAuditService.record(
                    tenantId,
                    actorId,
                    assignment.getUserId(),
                    assignment.getRoleId(),
                    "BULK_ROLE_ASSIGNED",
                    request.reason()
            );
            results.add(new BulkOperationItemResult(
                    assignment.getUserId(),
                    "ASSIGNED",
                    "Role " + role.getRoleCode() + " assigned successfully"
            ));
        }

        return summarize(request.userIds().size(), results, "ASSIGNED");
    }

    @Override
    @Transactional
    public BulkOperationResponse bulkRevoke(
            UUID tenantId,
            UUID actorId,
            BulkRoleRevokeRequest request
    ) {
        validateContext(tenantId, actorId);
        validateBulkSize(request.userIds().size(), "revoked");
        roleLookupService.getRole(tenantId, request.roleId());

        List<BulkOperationItemResult> results = new ArrayList<>();
        List<UserRole> changedAssignments = new ArrayList<>();
        LinkedHashSet<UUID> uniqueUsers = new LinkedHashSet<>();

        for (UUID userId : request.userIds()) {
            if (!uniqueUsers.add(userId)) {
                results.add(new BulkOperationItemResult(
                        userId, "SKIPPED", "Duplicate userId in request"));
            }
        }

        for (UUID userId : uniqueUsers) {
            try {
                Optional<UserRole> current =
                        userRoleRepository.findFirstByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                                tenantId, userId, request.roleId());

                if (current.isEmpty()) {
                    results.add(new BulkOperationItemResult(
                            userId, "SKIPPED", "Role is not currently assigned"));
                    continue;
                }

                UserRole assignment = current.get();
                validateRevokeBusinessRules(tenantId, assignment);
                assignment.setActive(false);
                assignment.setRevokedBy(actorId);
                assignment.setRevokedAt(LocalDateTime.now());
                assignment.setRevokeReason(request.reason());
                changedAssignments.add(assignment);
            } catch (RuntimeException ex) {
                if (atomicMode) {
                    throw ex;
                }
                results.add(new BulkOperationItemResult(userId, "FAILED", safeMessage(ex)));
            }
        }

        List<UserRole> saved = userRoleRepository.saveAll(changedAssignments);
        for (UserRole assignment : saved) {
            roleAuditService.record(
                    tenantId,
                    actorId,
                    assignment.getUserId(),
                    assignment.getRoleId(),
                    "BULK_ROLE_REVOKED",
                    request.reason()
            );
            results.add(new BulkOperationItemResult(
                    assignment.getUserId(), "REVOKED", "Role revoked successfully"));
        }

        return summarize(request.userIds().size(), results, "REVOKED");
    }

    private void validateAssignmentRequest(BulkRoleAssignmentRequest request) {
        validateBulkSize(request.userIds().size(), "assigned");
        if (request.effectiveDate().isBefore(LocalDate.now())) {
            throw new RoleAssignmentValidationException("effectiveDate cannot be in the past");
        }
        if (request.expiryDate() != null
                && !request.expiryDate().isAfter(request.effectiveDate())) {
            throw new RoleAssignmentValidationException(
                    "expiryDate must be after effectiveDate");
        }
    }

    private void validateRevokeBusinessRules(UUID tenantId, UserRole assignment) {
        LocalDate today = LocalDate.now();
        if (assignment.isPrimary()) {
            throw new RoleAssignmentValidationException(
                    "Cannot revoke a primary role in bulk; set another primary role first");
        }

        boolean currentlyEffective = !assignment.getEffectiveDate().isAfter(today)
                && (assignment.getExpiryDate() == null
                || !assignment.getExpiryDate().isBefore(today));

        if (currentlyEffective) {
            long currentRoles = userRoleRepository.countCurrentActiveAssignmentsForUser(
                    tenantId, assignment.getUserId(), today);
            if (currentRoles <= 1) {
                throw new RoleAssignmentValidationException(
                        "A user must always have at least one active role");
            }
        }
    }

    private void validateBulkSize(int size, String operation) {
        if (size > MAX_BULK_SIZE) {
            throw new RoleAssignmentValidationException(
                    "A maximum of 500 users can be " + operation + " in one request");
        }
    }

    private void validateContext(UUID tenantId, UUID actorId) {
        if (tenantId == null) {
            throw new RoleAssignmentValidationException("Tenant context is required");
        }
        if (actorId == null) {
            throw new RoleAssignmentValidationException("Authenticated actor context is required");
        }
    }

    private BulkOperationResponse summarize(
            int requestedCount,
            List<BulkOperationItemResult> results,
            String successStatus
    ) {
        int success = (int) results.stream()
                .filter(result -> successStatus.equals(result.status()))
                .count();
        int skipped = (int) results.stream()
                .filter(result -> "SKIPPED".equals(result.status()))
                .count();
        int failed = (int) results.stream()
                .filter(result -> "FAILED".equals(result.status()))
                .count();

        return new BulkOperationResponse(
                requestedCount,
                success,
                skipped,
                failed,
                List.copyOf(results)
        );
    }

    private String safeMessage(RuntimeException ex) {
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }
}

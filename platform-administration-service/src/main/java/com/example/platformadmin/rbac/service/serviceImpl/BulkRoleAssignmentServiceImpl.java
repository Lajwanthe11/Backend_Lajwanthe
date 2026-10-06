package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.service.RoleAuditService;
import com.example.platformadmin.rbac.service.UserAssignmentSupportService;
import com.example.platformadmin.rbac.dto.response.BulkOperationItemResult;
import com.example.platformadmin.rbac.dto.response.BulkOperationResponse;
import com.example.platformadmin.rbac.dto.request.BulkRoleAssignmentRequest;
import com.example.platformadmin.rbac.dto.request.BulkRoleRevokeRequest;
import com.example.platformadmin.rbac.entity.Role;
import com.example.platformadmin.rbac.entity.UserRole;
import com.example.platformadmin.rbac.exception.RoleAssignmentValidationException;
import com.example.platformadmin.rbac.repository.UserRoleRepository;
import com.example.platformadmin.rbac.service.BulkRoleAssignmentService;
import com.example.platformadmin.rbac.service.RoleLookupService;
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
    // Sprint limit: large requests are capped to avoid expensive bulk operations.
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
        // Fail early before touching the database when request context or dates are invalid.
        validateContext(tenantId, actorId);
        validateAssignmentRequest(request);

        Role role = roleLookupService.getAssignableRole(tenantId, request.roleId());
        List<BulkOperationItemResult> results = new ArrayList<>();
        List<UserRole> stagedAssignments = new ArrayList<>();
// LinkedHashSet removes duplicate users but keeps the original request order for the response.
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
                // Existing active assignments are reported as skipped instead of creating duplicates.
                if (userRoleRepository.existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId, userId, request.roleId())) {
                    results.add(new BulkOperationItemResult(
                            userId, "SKIPPED", "already assigned"));
                    continue;
                }
// Stage assignments first so saveAll can persist the successful set together.
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
                // Atomic mode fails the whole operation; partial mode records the user-level failure and continues.
                if (atomicMode) {
                    throw ex;
                }
                results.add(new BulkOperationItemResult(userId, "FAILED", safeMessage(ex)));
            }
        }
        // Audit only records assignments that were actually persisted.
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
        // Revoke requests use the same tenant and actor checks as assignment requests.
        validateContext(tenantId, actorId);
        validateBulkSize(request.userIds().size(), "revoked");
        roleLookupService.getRole(tenantId, request.roleId());

        List<BulkOperationItemResult> results = new ArrayList<>();
        List<UserRole> changedAssignments = new ArrayList<>();
        // Duplicate user IDs are ignored after the first occurrence but still reported to the caller.
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
// Missing assignments are not treated as errors; there is simply nothing to revoke.
                if (current.isEmpty()) {
                    results.add(new BulkOperationItemResult(
                            userId, "SKIPPED", "Role is not currently assigned"));
                    continue;
                }

                UserRole assignment = current.get();
                // Protect primary and last-active-role rules before changing the assignment state.
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
// Persist revocation metadata together, then create the audit entries.
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
// Only count roles that are effective today when checking the "last active role" rule.
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

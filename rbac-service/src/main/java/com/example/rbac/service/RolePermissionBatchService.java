package com.example.rbac.service;

import com.example.rbac.dto.BatchPermissionUpdateRequest;
import com.example.rbac.dto.BatchPermissionUpdateResponse;
import com.example.rbac.dto.PermissionGrantRequest;
import com.example.rbac.entity.Role;
import com.example.rbac.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolePermissionBatchService {

    private final RoleRepository roleRepository;
    private final RolePermissionService rolePermissionService;

    @Transactional
    public BatchPermissionUpdateResponse updatePermissions(
            Long roleId,
            BatchPermissionUpdateRequest request,
            String userId
    ) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found"
                        )
                );

        Long currentVersion = role.getVersion();

        /*
         * Optimistic locking check.
         */
        if (currentVersion != null
                && !currentVersion.equals(
                        request.getExpectedVersion()
                )) {

            throw new ObjectOptimisticLockingFailureException(
                    Role.class,
                    roleId
            );
        }

        int updatedCount = 0;

        /*
         * All permission changes execute inside
         * the same transaction.
         *
         * If any operation fails, the complete
         * transaction rolls back.
         */
        for (PermissionGrantRequest permission :
                request.getPermissions()) {

            if (Boolean.TRUE.equals(
                    permission.getGranted()
            )) {

                rolePermissionService.grantPermission(
                        roleId,
                        permission.getPermissionId(),
                        userId
                );

            } else {

                rolePermissionService.revokePermission(
                        roleId,
                        permission.getPermissionId(),
                        userId
                );
            }

            updatedCount++;
        }

        /*
         * Force Role to become dirty so Hibernate
         * performs the optimistic-lock version update.
         */
        role.setUpdatedBy(userId);

        try {

            role = roleRepository.saveAndFlush(role);

        } catch (ObjectOptimisticLockingFailureException ex) {

            throw new ObjectOptimisticLockingFailureException(
                    Role.class,
                    roleId
            );
        }

        return BatchPermissionUpdateResponse.builder()
                .roleId(roleId)
                .updatedCount(updatedCount)
                .version(role.getVersion())
                .message(
                        "Permissions updated successfully"
                )
                .build();
    }
}
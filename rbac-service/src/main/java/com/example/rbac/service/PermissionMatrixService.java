package com.example.rbac.service;

import com.example.rbac.dto.PermissionMatrixResponse;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.PermissionGroup;
import com.example.rbac.entity.Role;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionMatrixService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public PermissionMatrixResponse getMatrix(String tenantId) {

        List<Role> roles =
                roleRepository.findByTenantIdAndIsDeletedFalse(tenantId);

        List<Permission> permissions =
                permissionRepository
                        .findByActiveTrueOrderByGroup_DisplayOrderAscDisplayNameAsc();

        List<Long> roleIds =
                roles.stream()
                        .map(Role::getId)
                        .toList();

        Map<Long, Map<UUID, Boolean>> roleGrants =
                new LinkedHashMap<>();

        if (!roleIds.isEmpty()) {

            rolePermissionRepository
                    .findByRole_IdInAndActiveTrue(roleIds)
                    .forEach(rolePermission -> {

                        Long roleId =
                                rolePermission.getRole().getId();

                        UUID permissionId =
                                rolePermission
                                        .getPermission()
                                        .getPermissionId();

                        roleGrants
                                .computeIfAbsent(
                                        roleId,
                                        key -> new LinkedHashMap<>()
                                )
                                .put(permissionId, true);
                    });
        }

        List<PermissionMatrixResponse.RoleColumn> roleColumns =
                roles.stream()
                        .map(role ->
                                PermissionMatrixResponse.RoleColumn.builder()
                                        .roleId(role.getId())
                                        .roleName(role.getRoleName())
                                        .roleCode(role.getRoleCode())
                                        .version(role.getVersion())
                                        .build()
                        )
                        .toList();

        Map<PermissionGroup, List<Permission>> groupedPermissions =
                permissions.stream()
                        .filter(permission ->
                                permission.getGroup() != null
                        )
                        .collect(Collectors.groupingBy(
                                Permission::getGroup,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<PermissionMatrixResponse.PermissionGroupRow> groups =
                groupedPermissions.entrySet()
                        .stream()
                        .map(entry ->
                                buildPermissionGroup(
                                        entry.getKey(),
                                        entry.getValue(),
                                        roles,
                                        roleGrants
                                )
                        )
                        .toList();

        return PermissionMatrixResponse.builder()
                .roles(roleColumns)
                .permissionGroups(groups)
                .build();
    }

    public List<PermissionMatrixResponse.PermissionGroupRow>
    getGroupedPermissions(Long roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Role not found"));

        List<Permission> permissions =
                permissionRepository
                        .findByActiveTrueOrderByGroup_DisplayOrderAscDisplayNameAsc();

        Map<UUID, Boolean> grants =
                rolePermissionRepository
                        .findByRole_IdAndActiveTrue(roleId)
                        .stream()
                        .collect(Collectors.toMap(
                                rp -> rp.getPermission().getPermissionId(),
                                rp -> true,
                                (existing, replacement) -> replacement
                        ));

        Map<PermissionGroup, List<Permission>> grouped =
                permissions.stream()
                        .filter(permission ->
                                permission.getGroup() != null
                        )
                        .collect(Collectors.groupingBy(
                                Permission::getGroup,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        return grouped.entrySet()
                .stream()
                .map(entry -> {

                    List<PermissionMatrixResponse.PermissionRow> rows =
                            entry.getValue()
                                    .stream()
                                    .map(permission ->
                                            PermissionMatrixResponse.PermissionRow
                                                    .builder()
                                                    .permId(
                                                            permission
                                                                    .getPermissionId()
                                                                    .toString()
                                                    )
                                                    .permCode(
                                                            permission
                                                                    .getPermissionCode()
                                                    )
                                                    .displayName(
                                                            permission
                                                                    .getDisplayName()
                                                    )
                                                    .system(
                                                            permission.isSystem()
                                                    )
                                                    .roleGrants(
                                                            Map.of(
                                                                    role.getId()
                                                                            .toString(),
                                                                    grants.getOrDefault(
                                                                            permission
                                                                                    .getPermissionId(),
                                                                            false
                                                                    )
                                                            )
                                                    )
                                                    .build()
                                    )
                                    .toList();

                    return PermissionMatrixResponse.PermissionGroupRow
                            .builder()
                            .groupId(
                                    entry.getKey()
                                            .getGroupId()
                                            .toString()
                            )
                            .groupName(
                                    entry.getKey().getGroupName()
                            )
                            .permissions(rows)
                            .build();
                })
                .toList();
    }

    private PermissionMatrixResponse.PermissionGroupRow
    buildPermissionGroup(
            PermissionGroup group,
            List<Permission> permissions,
            List<Role> roles,
            Map<Long, Map<UUID, Boolean>> roleGrants
    ) {

        List<PermissionMatrixResponse.PermissionRow> rows =
                permissions.stream()
                        .map(permission ->
                                buildPermissionRow(
                                        permission,
                                        roles,
                                        roleGrants
                                )
                        )
                        .toList();

        return PermissionMatrixResponse.PermissionGroupRow
                .builder()
                .groupId(group.getGroupId().toString())
                .groupName(group.getGroupName())
                .permissions(rows)
                .build();
    }

    private PermissionMatrixResponse.PermissionRow
    buildPermissionRow(
            Permission permission,
            List<Role> roles,
            Map<Long, Map<UUID, Boolean>> roleGrants
    ) {

        Map<String, Boolean> grants =
                new LinkedHashMap<>();

        for (Role role : roles) {

            boolean granted =
                    roleGrants
                            .getOrDefault(
                                    role.getId(),
                                    Map.of()
                            )
                            .getOrDefault(
                                    permission.getPermissionId(),
                                    false
                            );

            grants.put(
                    role.getId().toString(),
                    granted
            );
        }

        return PermissionMatrixResponse.PermissionRow
                .builder()
                .permId(
                        permission.getPermissionId().toString()
                )
                .permCode(
                        permission.getPermissionCode()
                )
                .displayName(
                        permission.getDisplayName()
                )
                .roleGrants(grants)
                .system(permission.isSystem())
                .build();
    }
}
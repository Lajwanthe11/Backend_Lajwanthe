package com.example.rbac.service.serviceImpl;

import com.example.common.exception.BadRequestException;
import com.example.rbac.dto.DeptScopeReportDto;
import com.example.rbac.entity.RoleDepartmentMap;
import com.example.rbac.repository.RoleDepartmentMapRepository;
import com.example.rbac.service.DepartmentPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.rbac.entity.UserRole;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.UserRoleRepository;


import java.util.List;
import java.util.UUID;

@Service
public class DepartmentPermissionServiceImpl implements DepartmentPermissionService {

    private final RoleDepartmentMapRepository repository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public DepartmentPermissionServiceImpl(
            RoleDepartmentMapRepository repository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository) {
        this.repository = repository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getDepartmentScope(UUID userRoleId) {
        return repository.findByUserRoleId(userRoleId)
                .stream()
                .map(RoleDepartmentMap::getDepartmentId)
                .toList();
    }

    @Override
    @Transactional
    public void updateDepartmentScope(UUID userRoleId, List<UUID> departmentIds) {

        if (departmentIds == null || departmentIds.isEmpty()) {
            throw new BadRequestException("Department list cannot be empty");
        }

        if (departmentIds.contains(null)) {
            throw new BadRequestException("Department list cannot contain null values");
        }

        if (departmentIds.size() != new java.util.HashSet<>(departmentIds).size()) {
            throw new BadRequestException("Department list cannot contain duplicate values");
        }

        repository.deleteByUserRoleId(userRoleId);

        for (UUID deptId : departmentIds) {
            RoleDepartmentMap map = new RoleDepartmentMap();
            map.setUserRoleId(userRoleId);
            map.setDepartmentId(deptId);
            repository.save(map);
        }
    }

    @Override
    @Transactional
    public void removeAllDepartmentScope(UUID userRoleId) {
        repository.deleteByUserRoleId(userRoleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeptScopeReportDto> getDeptScopeReport() {
        return repository.findAll()
                .stream()
                .map(map -> new DeptScopeReportDto(
                        map.getUserRoleId(),
                        map.getDepartmentId(),
                        map.getCreatedAt(),
                        map.getCreatedBy()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getUsersByPermission(UUID departmentId, String permissionCode) {

        List<RoleDepartmentMap> mappings = repository.findByDepartmentId(departmentId);

        List<UUID> userRoleIds = mappings.stream()
                .map(RoleDepartmentMap::getUserRoleId)
                .toList();

        if (userRoleIds.isEmpty()) {
            return List.of();
        }

        List<UserRole> userRoles = userRoleRepository.findAllById(userRoleIds);

        return userRoles.stream()
                .filter(ur -> rolePermissionRepository
                        .findByRole_IdAndActiveTrue(ur.getRoleId())
                        .stream()
                        .anyMatch(rp -> rp.getPermission().getPermissionCode().equals(permissionCode)))
                .map(UserRole::getUserId)
                .distinct()
                .toList();
    }
}
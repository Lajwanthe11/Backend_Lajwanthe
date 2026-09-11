package com.example.rbac.service.serviceImpl;

import com.example.common.exception.BadRequestException;
import com.example.rbac.dto.DeptScopeReportDto;
import com.example.rbac.entity.RoleDepartmentMap;
import com.example.rbac.repository.RoleDepartmentMapRepository;
import com.example.rbac.service.DepartmentPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentPermissionServiceImpl implements DepartmentPermissionService {

    private final RoleDepartmentMapRepository repository;

    public DepartmentPermissionServiceImpl(RoleDepartmentMapRepository repository) {
        this.repository = repository;
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
}
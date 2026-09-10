package com.example.rbac.service.serviceImpl;

import com.example.common.exception.BadRequestException;
import com.example.rbac.entity.RoleDepartmentMap;
import com.example.rbac.repository.RoleDepartmentMapRepository;
import com.example.rbac.service.DepartmentPermissionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentPermissionServiceImpl implements DepartmentPermissionService {

    private final RoleDepartmentMapRepository repository;

    public DepartmentPermissionServiceImpl(RoleDepartmentMapRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getDepartmentScope(Long userRoleId) {
        return repository.findByUserRoleId(userRoleId)
                .stream()
                .map(RoleDepartmentMap::getDepartmentId)
                .toList();
    }

    @Override
    @Transactional
    public void updateDepartmentScope(Long userRoleId, List<Long> departmentIds) {

        if (departmentIds == null || departmentIds.isEmpty()) {
            throw new BadRequestException("Department list cannot be empty");
        }

        // Remove old scope first
        repository.deleteByUserRoleId(userRoleId);

        // Save new scope
        for (Long deptId : departmentIds) {
            RoleDepartmentMap map = new RoleDepartmentMap();
            map.setUserRoleId(userRoleId);
            map.setDepartmentId(deptId);
            repository.save(map);
        }
    }

    @Override
    @Transactional
    public void removeAllDepartmentScope(Long userRoleId) {
        repository.deleteByUserRoleId(userRoleId);
    }
}
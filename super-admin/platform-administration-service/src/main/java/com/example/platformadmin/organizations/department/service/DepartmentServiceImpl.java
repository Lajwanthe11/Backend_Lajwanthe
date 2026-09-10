package com.example.platformadmin.organizations.department.service;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.department.dto.DepartmentRequest;
import com.example.platformadmin.organizations.department.dto.DepartmentResponse;
import com.example.platformadmin.organizations.department.entity.Department;
import com.example.platformadmin.organizations.department.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of department CRUD using AbstractService from base-service.
 */
@Service
public class DepartmentServiceImpl
        extends AbstractService<Department, Long, DepartmentRequest, DepartmentResponse>
        implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        super(departmentRepository, "Department");
        this.departmentRepository = departmentRepository;
    }

    @Override
    protected Department toEntity(DepartmentRequest dto) {
        Department department = new Department();
        department.setDepartmentCode(dto.getDepartmentCode());
        department.setDepartmentName(dto.getDepartmentName());
        department.setDescription(dto.getDescription());
        department.setActive(dto.getActive());
        return department;
    }

    @Override
    protected DepartmentResponse toDto(Department entity) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(entity.getId());
        response.setDepartmentCode(entity.getDepartmentCode());
        response.setDepartmentName(entity.getDepartmentName());
        response.setDescription(entity.getDescription());
        response.setActive(entity.getActive());
        return response;
    }

    @Override
    protected void updateEntityFromDto(Department entity, DepartmentRequest dto) {
        entity.setDepartmentCode(dto.getDepartmentCode());
        entity.setDepartmentName(dto.getDepartmentName());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.getActive());
    }

    @Override
    public List<DepartmentResponse> searchDepartments(String departmentName) {

        List<DepartmentResponse> departments = departmentRepository
                .findByDepartmentNameContainingIgnoreCase(departmentName)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        if (departments.isEmpty()) {
            throw new RuntimeException("Department not found");
        }

        return departments;
    }
}

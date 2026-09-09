package com.example.rbac.service;

import com.example.common.abstracts.AbstractService;
import com.example.common.exception.BadRequestException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.rbac.dto.RoleRequestDto;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.entity.Role;
import com.example.rbac.repository.RoleRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleServiceImpl
        extends AbstractService<Role, Long, RoleRequestDto, RoleResponseDto>
        implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        super(roleRepository, "Role");
        this.roleRepository = roleRepository;
    }

    // Convert request DTO to Role entity
    @Override
    protected Role toEntity(RoleRequestDto dto) {

        Role role = new Role();

        role.setRoleName(dto.getRoleName());

        String roleCode = dto.getRoleCode();

        if (roleCode == null || roleCode.isBlank()) {
            roleCode = dto.getRoleName()
                    .trim()
                    .toUpperCase()
                    .replaceAll("[^A-Z0-9]+", "_");
        }

        role.setRoleCode(roleCode);
        role.setRoleType(dto.getRoleType());
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        role.setIsDeleted(false);

        return role;
    }

    // Convert Role entity to response DTO
    @Override
    protected RoleResponseDto toDto(Role role) {

        return new RoleResponseDto(
                role.getId(),
                role.getRoleName(),
                role.getRoleCode(),
                role.getRoleType(),
                role.getDescription(),
                role.getStatus(),
                role.getIsDeleted(),
                role.getCreatedAt(),
                role.getCreatedBy(),
                role.getUpdatedAt(),
                role.getUpdatedBy()
        );
    }

    // Update editable Role fields
    @Override
    protected void updateEntityFromDto(Role role, RoleRequestDto dto) {
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
    }

    // Check duplicate role details within current tenant
    @Override
    protected void beforeCreate(Role role, RoleRequestDto dto) {

        String tenantId = getCurrentTenantId();

        if (roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        role.getRoleName(), tenantId)) {

            throw new BadRequestException("Role name already exists");
        }

        if (roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        role.getRoleCode(), tenantId)) {

            throw new BadRequestException("Role code already exists");
        }
    }

    // Search roles for current tenant
    @Override
    public List<RoleResponseDto> searchRoles(String query) {

        return roleRepository
                .searchRoles(getCurrentTenantId(), query)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Update role within current tenant
    @Override
    @Transactional
    public RoleResponseDto update(Long id, RoleRequestDto dto) {

        validateId(id);

        Role role = roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        id, getCurrentTenantId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role", "id", id));

        if (!role.getRoleName().equalsIgnoreCase(dto.getRoleName())
                && roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        dto.getRoleName(),
                        getCurrentTenantId())) {

            throw new BadRequestException(
                    "Role name already exists");
        }

        updateEntityFromDto(role, dto);

        return toDto(roleRepository.save(role));
    }

    // Soft delete role for current tenant
    @Override
    @Transactional
    public void deleteById(Long id) {

        validateId(id);

        Role role = roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        id, getCurrentTenantId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role", "id", id));

        if (isProtectedSystemRole(role)) {
            throw new BadRequestException(
                    "System role cannot be deleted");
        }

        role.setIsDeleted(true);
        roleRepository.save(role);
    }

    // Activate or deactivate role
    @Override
    @Transactional
    public RoleResponseDto updateStatus(Long id, String status) {

        if (status == null
                || (!status.equalsIgnoreCase("ACTIVE")
                && !status.equalsIgnoreCase("INACTIVE"))) {

            throw new BadRequestException(
                    "Status must be ACTIVE or INACTIVE");
        }

        Role role = roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        id, getCurrentTenantId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role", "id", id));

        if (isProtectedSystemRole(role)
                && status.equalsIgnoreCase("INACTIVE")) {

            throw new BadRequestException(
                    "System role cannot be deactivated");
        }

        role.setStatus(status.toUpperCase());

        return toDto(roleRepository.save(role));
    }

    // Get role by tenant
    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto getById(Long id) {

        validateId(id);

        Role role = roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        id, getCurrentTenantId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role", "id", id));

        return toDto(role);
    }

    // Get all roles for current tenant
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getAll() {

        return roleRepository
                .findByTenantIdAndIsDeletedFalse(
                        getCurrentTenantId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Get paginated roles for current tenant
    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponseDto> getAll(Pageable pageable) {

        return roleRepository
                .findByTenantIdAndIsDeletedFalse(
                        getCurrentTenantId(), pageable)
                .map(this::toDto);
    }

    // Check protected system roles
    private boolean isProtectedSystemRole(Role role) {

        String code = role.getRoleCode();

        return "SUPER_ADMIN".equalsIgnoreCase(code)
                || "ADMIN".equalsIgnoreCase(code)
                || "EMPLOYEE".equalsIgnoreCase(code);
    }
}
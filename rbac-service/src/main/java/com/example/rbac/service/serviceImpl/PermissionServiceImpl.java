package com.example.rbac.service.serviceImpl;

import com.example.rbac.dto.PermissionResponseDto;
import com.example.rbac.entity.Permission;
import com.example.rbac.exception.PermissionNotFoundException;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Page<PermissionResponseDto> listPermissions(UUID groupId, String module, boolean activeOnly, Pageable pageable) {
        return permissionRepository.filter(groupId, module, activeOnly, pageable).map(this::toDto);
    }

    @Override
    public PermissionResponseDto getById(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));
        return toDto(permission);
    }

    @Override
    public List<PermissionResponseDto> search(String term) {
        return permissionRepository.search(term).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<PermissionResponseDto> getByModule(String module) {
        return permissionRepository.findByModuleIgnoreCaseAndActiveTrue(module).stream()
                .map(this::toDto)
                .toList();
    }

    private PermissionResponseDto toDto(Permission p) {
        return new PermissionResponseDto(
                p.getPermissionId(),
                p.getPermissionCode(),
                p.getResource(),
                p.getAction(),
                p.getGroup() != null ? p.getGroup().getGroupId() : null,
                p.getGroup() != null ? p.getGroup().getGroupName() : null,
                p.getDisplayName(),
                p.getDescription(),
                p.isActive(),
                p.isSystem(),
                p.getModule()
        );
    }
}

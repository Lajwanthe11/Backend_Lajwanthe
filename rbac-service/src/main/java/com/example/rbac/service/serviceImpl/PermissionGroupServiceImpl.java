package com.example.rbac.service.serviceImpl;

import com.example.rbac.dto.PermissionGroupResponseDto;
import com.example.rbac.dto.PermissionResponseDto;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.PermissionGroup;
import com.example.rbac.repository.PermissionGroupRepository;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.service.PermissionGroupNotFoundException;
import com.example.rbac.service.PermissionGroupService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PermissionGroupServiceImpl implements PermissionGroupService {

    private final PermissionGroupRepository groupRepository;
    private final PermissionRepository permissionRepository;

    public PermissionGroupServiceImpl(PermissionGroupRepository groupRepository,
            PermissionRepository permissionRepository) {
        this.groupRepository = groupRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<PermissionGroupResponseDto> listGroups() {
        return groupRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public PermissionGroupResponseDto getGroupWithPermissions(UUID groupId) {
        PermissionGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new PermissionGroupNotFoundException(groupId));

        PermissionGroupResponseDto dto = toDto(group);
        List<PermissionResponseDto> permissions = permissionRepository
                .findByGroup_GroupIdAndActiveTrue(groupId).stream()
                .map(this::toPermissionDto)
                .toList();
        dto.setPermissions(permissions);
        return dto;
    }

    private PermissionGroupResponseDto toDto(PermissionGroup g) {
        return new PermissionGroupResponseDto(
                g.getGroupId(), g.getGroupName(), g.getGroupCode(),
                g.getModule(), g.getDisplayOrder(), g.isActive());
    }

    private PermissionResponseDto toPermissionDto(Permission p) {
        return new PermissionResponseDto(
                p.getPermissionId(), p.getPermissionCode(), p.getResource(), p.getAction(),
                p.getGroup() != null ? p.getGroup().getGroupId() : null,
                p.getGroup() != null ? p.getGroup().getGroupName() : null,
                p.getDisplayName(), p.getDescription(), p.isActive(), p.isSystem(), p.getModule());
    }
}

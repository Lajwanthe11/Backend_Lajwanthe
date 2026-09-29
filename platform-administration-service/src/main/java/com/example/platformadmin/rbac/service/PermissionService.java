package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.dto.response.PermissionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PermissionService {

    Page<PermissionResponseDto> listPermissions(UUID groupId, String module, boolean activeOnly, Pageable pageable);

    PermissionResponseDto getById(UUID permissionId);

    List<PermissionResponseDto> search(String term);

    List<PermissionResponseDto> getByModule(String module);
}

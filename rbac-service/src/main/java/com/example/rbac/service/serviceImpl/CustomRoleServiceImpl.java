// CustomRoleServiceImpl.java

package com.example.rbac.service.serviceImpl;

import com.example.common.tenant.TenantContext;
import com.example.rbac.dto.CustomRoleRequest;
import com.example.rbac.dto.CustomRoleResponse;
import com.example.rbac.entity.CustomRoleConfig;
import com.example.rbac.entity.CustomRoleVersion;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.CustomRoleStatus;
import com.example.rbac.enums.RoleType;
import com.example.rbac.repository.CustomRoleConfigRepository;
import com.example.rbac.repository.CustomRoleDataRepository;
import com.example.rbac.repository.CustomRoleVersionRepository;
import com.example.rbac.service.CustomRoleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class CustomRoleServiceImpl implements CustomRoleService {

    private final CustomRoleDataRepository roleRepository;
    private final CustomRoleConfigRepository configRepository;
    private final CustomRoleVersionRepository versionRepository;
    private final ObjectMapper objectMapper;

    public CustomRoleServiceImpl(CustomRoleDataRepository roleRepository, CustomRoleConfigRepository configRepository, CustomRoleVersionRepository versionRepository, ObjectMapper objectMapper) {

        this.roleRepository = roleRepository;
        this.configRepository = configRepository;
        this.versionRepository = versionRepository;
        this.objectMapper = objectMapper;
    }

    private String tenant() {

        String tenantId = TenantContext.getTenantId();

        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Tenant context is not available");
        }

        return tenantId;
    }

    private String currentUser() {
        return "SYSTEM";
    }

    @Override
    public CustomRoleResponse create(CustomRoleRequest request) {

        String tenantId = tenant();

        if (roleRepository.existsByRoleNameIgnoreCaseAndTenantId(request.getRoleName(), tenantId)) {

            throw new IllegalArgumentException("Role name already exists");
        }

        String roleCode = request.getRoleCode();

        if (roleCode == null || roleCode.isBlank()) {

            roleCode = request.getRoleName().trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_");
        }

        if (roleRepository.existsByRoleCodeIgnoreCaseAndTenantId(roleCode, tenantId)) {

            throw new IllegalArgumentException("Role code already exists");
        }

        Role role = new Role();

        role.setRoleName(request.getRoleName().trim());

        role.setRoleCode(roleCode.trim().toUpperCase());

        role.setRoleType(RoleType.CUSTOM);

        role.setDescription(request.getDescription());

        role.setStatus("DRAFT");

        role.setIsDeleted(false);

        role.setTenantId(UUID.fromString(tenantId));

        role = roleRepository.save(role);

        CustomRoleConfig config = new CustomRoleConfig();

        config.setRoleId(role.getId());

        config.setTenantId(tenantId);

        config.setDraftVersion(1);

        config.setPublishedVersion(0);

        config.setStatus(CustomRoleStatus.DRAFT);

        config.setCreatedBy(currentUser());

        config.setCreatedAt(LocalDateTime.now());

        config = configRepository.save(config);

        saveVersion(role.getId(), tenantId, 1, request.getPermissionIds(), CustomRoleStatus.DRAFT, request.getPublishNotes());

        return response(role, config, request.getPermissionIds());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomRoleResponse> getAll() {

        String tenantId = tenant();

        List<CustomRoleResponse> result = new ArrayList<>();

        List<CustomRoleConfig> configs = configRepository.findAllByTenantId(tenantId);

        for (CustomRoleConfig config : configs) {

            Role role = roleRepository.findByIdAndTenantId(config.getRoleId(), tenantId).orElse(null);

            if (role == null) {
                continue;
            }

            if (Boolean.TRUE.equals(role.getIsDeleted())) {
                continue;
            }

            Integer version;

            if (config.getStatus() == CustomRoleStatus.PUBLISHED) {

                version = config.getPublishedVersion();

            } else {

                version = config.getDraftVersion();
            }

            List<Long> permissions = getPermissions(role.getId(), tenantId, version);

            result.add(response(role, config, permissions));
        }

        return result;
    }

    @Override
    public CustomRoleResponse update(UUID roleId, CustomRoleRequest request) {

        String tenantId = tenant();

        Role role = getRole(roleId, tenantId);

        CustomRoleConfig config = getConfig(roleId, tenantId);

        if (config.getStatus() == CustomRoleStatus.ARCHIVED) {

            throw new IllegalStateException("Archived custom role cannot be updated");
        }

        if (!role.getRoleName().equalsIgnoreCase(request.getRoleName())) {

            if (roleRepository.existsByRoleNameIgnoreCaseAndTenantId(request.getRoleName(), tenantId)) {

                throw new IllegalArgumentException("Role name already exists");
            }

            role.setRoleName(request.getRoleName().trim());
        }

        role.setDescription(request.getDescription());

        if (request.getRoleCode() != null && !request.getRoleCode().isBlank() && !role.getRoleCode().equalsIgnoreCase(request.getRoleCode())) {

            if (roleRepository.existsByRoleCodeIgnoreCaseAndTenantId(request.getRoleCode(), tenantId)) {

                throw new IllegalArgumentException("Role code already exists");
            }

            role.setRoleCode(request.getRoleCode().trim().toUpperCase());
        }

        int version = nextVersion(roleId, tenantId);

        saveVersion(roleId, tenantId, version, request.getPermissionIds(), CustomRoleStatus.DRAFT, request.getPublishNotes());

        config.setDraftVersion(version);

        config.setStatus(CustomRoleStatus.DRAFT);

        config.setPublishNotes(request.getPublishNotes());

        if (config.getPublishedVersion() > 0) {

            role.setStatus("ACTIVE");

        } else {

            role.setStatus("DRAFT");
        }

        roleRepository.save(role);

        configRepository.save(config);

        return response(role, config, request.getPermissionIds());
    }

    @Override
    public CustomRoleResponse publish(UUID roleId, String publishNotes) {

        String tenantId = tenant();

        Role role = getRole(roleId, tenantId);

        CustomRoleConfig config = getConfig(roleId, tenantId);

        if (config.getStatus() == CustomRoleStatus.ARCHIVED) {

            throw new IllegalStateException("Archived custom role cannot be published");
        }

        CustomRoleVersion version = versionRepository.findByRoleIdAndTenantIdAndVersionNumber(roleId, tenantId, config.getDraftVersion()).orElseThrow(() -> new IllegalStateException("Draft version not found"));

        if (version.getStatus() != CustomRoleStatus.DRAFT) {

            throw new IllegalStateException("Only a draft version can be published");
        }

        LocalDateTime now = LocalDateTime.now();

        version.setStatus(CustomRoleStatus.PUBLISHED);

        version.setPublishedBy(currentUser());

        version.setPublishedAt(now);

        version.setPublishNotes(publishNotes);

        versionRepository.save(version);

        config.setPublishedVersion(config.getDraftVersion());

        config.setStatus(CustomRoleStatus.PUBLISHED);

        config.setPublishNotes(publishNotes);

        config.setPublishedBy(currentUser());

        config.setPublishedAt(now);

        configRepository.save(config);

        role.setStatus("ACTIVE");

        roleRepository.save(role);

        return response(role, config, readPermissions(version.getPermissionSnapshot()));
    }

    @Override
    public CustomRoleResponse archive(UUID roleId) {

        String tenantId = tenant();

        Role role = getRole(roleId, tenantId);

        CustomRoleConfig config = getConfig(roleId, tenantId);

        config.setStatus(CustomRoleStatus.ARCHIVED);

        configRepository.save(config);

        role.setStatus("ARCHIVED");

        role.setIsDeleted(true);

        roleRepository.save(role);

        return response(role, config, getPermissions(roleId, tenantId, config.getDraftVersion()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomRoleResponse> getVersions(UUID roleId) {

        String tenantId = tenant();

        Role role = getRole(roleId, tenantId);

        CustomRoleConfig config = getConfig(roleId, tenantId);

        List<CustomRoleResponse> result = new ArrayList<>();

        List<CustomRoleVersion> versions = versionRepository.findAllByRoleIdAndTenantIdOrderByVersionNumberDesc(roleId, tenantId);

        for (CustomRoleVersion version : versions) {

            CustomRoleResponse response = response(role, config, readPermissions(version.getPermissionSnapshot()));

            response.setVersionNumber(version.getVersionNumber());

            response.setStatus(version.getStatus());

            response.setPublishNotes(version.getPublishNotes());

            response.setPublishedBy(version.getPublishedBy());

            response.setPublishedAt(version.getPublishedAt());

            result.add(response);
        }

        return result;
    }

    @Override
    public CustomRoleResponse revert(UUID roleId, Integer versionNumber) {

        String tenantId = tenant();

        Role role = getRole(roleId, tenantId);

        CustomRoleConfig config = getConfig(roleId, tenantId);

        CustomRoleVersion oldVersion = versionRepository.findByRoleIdAndTenantIdAndVersionNumber(roleId, tenantId, versionNumber).orElseThrow(() -> new IllegalArgumentException("Version not found"));

        List<Long> permissions = readPermissions(oldVersion.getPermissionSnapshot());

        int newVersion = nextVersion(roleId, tenantId);

        saveVersion(roleId, tenantId, newVersion, permissions, CustomRoleStatus.DRAFT, "Reverted from version " + versionNumber);

        config.setDraftVersion(newVersion);

        config.setStatus(CustomRoleStatus.DRAFT);

        configRepository.save(config);

        return response(role, config, permissions);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getImpact(UUID roleId) {

        getRole(roleId, tenant());

        return Map.of("roleId", roleId, "message", "Impact analysis integration is pending");
    }

    @Override
    @Transactional(readOnly = true)
    public Object getLimits() {

        String tenantId = tenant();

        long count = configRepository.countByTenantIdAndStatusNot(tenantId, CustomRoleStatus.ARCHIVED);

        return Map.of("tenantId", tenantId, "currentCustomRoles", count, "limit", "LICENSE_INTEGRATION_PENDING");
    }

    private void saveVersion(UUID roleId, String tenantId, int number, List<Long> permissions, CustomRoleStatus status, String notes) {

        CustomRoleVersion version = new CustomRoleVersion();

        version.setRoleId(roleId);

        version.setTenantId(tenantId);

        version.setVersionNumber(number);

        version.setStatus(status);

        version.setPermissionSnapshot(writePermissions(permissions));

        version.setCreatedBy(currentUser());

        version.setCreatedAt(LocalDateTime.now());

        version.setPublishNotes(notes);

        versionRepository.save(version);
    }

    private int nextVersion(UUID roleId, String tenantId) {

        return versionRepository.findTopByRoleIdAndTenantIdOrderByVersionNumberDesc(roleId, tenantId).map(version -> version.getVersionNumber() + 1).orElse(1);
    }

    private List<Long> getPermissions(UUID roleId, String tenantId, Integer version) {

        if (version == null || version == 0) {

            return new ArrayList<>();
        }

        return versionRepository.findByRoleIdAndTenantIdAndVersionNumber(roleId, tenantId, version).map(versionEntity -> readPermissions(versionEntity.getPermissionSnapshot())).orElseGet(ArrayList::new);
    }

    private Role getRole(UUID roleId, String tenantId) {

        return roleRepository.findByIdAndTenantId(roleId, tenantId).orElseThrow(() -> new IllegalArgumentException("Custom role not found"));
    }

    private CustomRoleConfig getConfig(UUID roleId, String tenantId) {

        return configRepository.findByRoleIdAndTenantId(roleId, tenantId).orElseThrow(() -> new IllegalArgumentException("Custom role configuration not found"));
    }

    private String writePermissions(List<Long> permissions) {

        try {

            return objectMapper.writeValueAsString(permissions == null ? List.of() : permissions);

        } catch (Exception e) {

            throw new IllegalStateException("Unable to save permission snapshot", e);
        }
    }

    private List<Long> readPermissions(String snapshot) {

        try {

            if (snapshot == null || snapshot.isBlank()) {

                return new ArrayList<>();
            }

            return objectMapper.readValue(snapshot, new TypeReference<List<Long>>() {
            });

        } catch (Exception e) {

            throw new IllegalStateException("Unable to read permission snapshot", e);
        }
    }

    private CustomRoleResponse response(Role role, CustomRoleConfig config, List<Long> permissions) {

        CustomRoleResponse response = new CustomRoleResponse();

        response.setRoleId(role.getId());

        response.setRoleName(role.getRoleName());

        response.setRoleCode(role.getRoleCode());

        response.setDescription(role.getDescription());

        response.setStatus(config.getStatus());

        response.setDraftVersion(config.getDraftVersion());

        response.setPublishedVersion(config.getPublishedVersion());

        response.setPermissionIds(permissions == null ? new ArrayList<>() : permissions);

        response.setPermissionCount(permissions == null ? 0 : permissions.size());

        response.setCreatedAt(role.getCreatedAt());

        return response;
    }
}
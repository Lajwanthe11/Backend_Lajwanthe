package com.example.rbac.service.serviceImpl;

import com.example.common.abstracts.AbstractService;
import com.example.common.exception.BadRequestException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.rbac.entity.RoleHistory;
import com.example.rbac.repository.RoleHistoryRepository;
import com.example.rbac.service.RoleExportService;
import com.example.rbac.service.RoleNotFoundException;
import com.example.rbac.dto.*;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RoleTemplate;
import com.example.rbac.enums.RoleType;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.RoleTemplateRepository;
import com.example.rbac.service.RoleService;
import com.example.rbac.service.CurrentUserContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends AbstractService<Role, Long, RoleRequestDto, RoleResponseDto>
        implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleTemplateRepository roleTemplateRepository;
    private final CurrentUserContext currentUser;
    private final RoleHistoryRepository roleHistoryRepository;
    private final RoleExportService roleExportService;

    public RoleServiceImpl(RoleRepository roleRepository,RoleTemplateRepository roleTemplateRepository,CurrentUserContext currentUser,RoleHistoryRepository roleHistoryRepository,RoleExportService roleExportService) {
        super(roleRepository, "Role");
        this.roleRepository = roleRepository;
        this.roleTemplateRepository = roleTemplateRepository;
        this.currentUser = currentUser;
        this.roleHistoryRepository = roleHistoryRepository;
        this.roleExportService = roleExportService;
    }

    // Convert request DTO to Role entity
    @Override
    protected Role toEntity(RoleRequestDto dto) {
        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        String roleCode = dto.getRoleCode();

        if (roleCode == null || roleCode.isBlank()) {
            roleCode = dto.getRoleName().trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_");
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
        return new RoleResponseDto(role.getId(),
                role.getRoleName(),
                role.getRoleCode(),
                role.getRoleType(),
                role.getDescription(),
                role.getStatus(),
                role.getIsDeleted(),
                role.getCreatedAt(),
                role.getCreatedBy(),
                role.getUpdatedAt(),
                role.getUpdatedBy());
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
    public List<RoleResponseDto> searchRoles(String query, RoleType roleType, String status) {

        return roleRepository.searchRoles(getCurrentTenantId(), query, roleType, status).stream().map(this::toDto)
                .toList();
    }

    // Update role within current tenant
    @Override
    @Transactional
    public RoleResponseDto update(Long id, RoleRequestDto dto) {
        validateId(id);
        Role role = roleRepository.findByIdAndTenantIdAndIsDeletedFalse(id, getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (!role.getRoleName().equalsIgnoreCase(dto.getRoleName()) && roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(dto.getRoleName(), getCurrentTenantId())) {
            throw new BadRequestException("Role name already exists");
        }

        updateEntityFromDto(role, dto);
        return toDto(roleRepository.save(role));
    }

    // Soft delete role for current tenant
    @Override
    @Transactional
    public void deleteById(Long id) {
        validateId(id);
        Role role = roleRepository.findByIdAndTenantIdAndIsDeletedFalse(id, getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (isProtectedSystemRole(role)) {
            throw new BadRequestException("System role cannot be deleted");
        }

        role.setIsDeleted(true);
        roleRepository.save(role);
    }

    // Activate or deactivate role
    @Override
    @Transactional
    public RoleResponseDto updateStatus(Long id, String status) {
        validateId(id);
        if (status == null
                || (!status.equalsIgnoreCase("ACTIVE")
                        && !status.equalsIgnoreCase("INACTIVE"))) {
            throw new BadRequestException("Status must be ACTIVE or INACTIVE");
        }

        Role role = roleRepository.findByIdAndTenantIdAndIsDeletedFalse(id, getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (isProtectedSystemRole(role) && status.equalsIgnoreCase("INACTIVE")) {
            throw new BadRequestException("System role cannot be deactivated");
        }

        role.setStatus(status.toUpperCase());

        return toDto(roleRepository.save(role));
    }

    // Get role by tenant
    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto getById(Long id) {
        validateId(id);

        Role role = roleRepository.findByIdAndTenantIdAndIsDeletedFalse(id, getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return toDto(role);
    }

    // Get all roles for current tenant
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getAll() {
        return roleRepository.findByTenantIdAndIsDeletedFalse(getCurrentTenantId()).stream().map(this::toDto).toList();
    }

    // Get paginated roles for current tenant
    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponseDto> getAll(Pageable pageable) {
        return roleRepository.findByTenantIdAndIsDeletedFalse(getCurrentTenantId(), pageable).map(this::toDto);
    }

    // Check protected system roles
    private boolean isProtectedSystemRole(Role role) {

        String code = role.getRoleCode();

        return "SUPER_ADMIN".equalsIgnoreCase(code) || "ADMIN".equalsIgnoreCase(code)
                || "EMPLOYEE".equalsIgnoreCase(code);
    }

    // Get role count badges for current tenant
    @Override
    public Map<String, Long> getRoleCounts() {

        String tenantId = getCurrentTenantId();

        long totalRoles = roleRepository.countByTenantIdAndIsDeletedFalse(tenantId);
        long systemRoles = roleRepository.countByTenantIdAndRoleTypeAndIsDeletedFalse(
                tenantId, RoleType.SYSTEM);

        long customRoles = roleRepository.countByTenantIdAndRoleTypeAndIsDeletedFalse(
                tenantId, RoleType.CUSTOM);

        return Map.of("totalRoles", totalRoles, "systemRoles", systemRoles, "customRoles", customRoles);
    }

    @Override
    public List<RoleTemplateSummaryDto> listTemplates() {
        // Super Admin sees hidden templates too (management view); everyone
        // else only sees the visible library.
        List<RoleTemplate> templates = currentUser.hasRole("SUPER_ADMIN")
                ? roleTemplateRepository.findAll()
                : roleTemplateRepository.findAllByHiddenFalse();

        return templates.stream()
                .map(t -> new RoleTemplateSummaryDto(
                        t.getId(),
                        t.getName(),
                        t.getDescription(),
                        t.getPermissions().size(),
                        t.getRecommendedFor()
                ))
                .toList();
    }

    @Override
    public RoleTemplateDetailDto getTemplateDetail(String templateId) {
        RoleTemplate template = roleTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("RoleTemplate", "id", templateId));

        Set<String> permissionCodes = template.getPermissions().stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toSet());

        return new RoleTemplateDetailDto(
                template.getId(),
                template.getName(),
                template.getDescription(),
                permissionCodes,
                template.getRecommendedFor()
        );
    }

    @Override
    public RoleCompareResponse compareRoles(String role1Id, String role2Id) {
        String tenantId = currentUser.getTenantId();

        Role role1 = roleRepository.findByIdAndTenantId(role1Id, tenantId)
                .orElseThrow(() -> new RoleNotFoundException(role1Id));
        Role role2 = roleRepository.findByIdAndTenantId(role2Id, tenantId)
                .orElseThrow(() -> new RoleNotFoundException(role2Id));

        // Permission sets default to empty (never null) so roles with zero
        // permissions compare cleanly instead of NPE-ing.
        Set<String> perms1 = role1.getPermissions().stream().map(Permission::getPermissionCode)
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> perms2 = role2.getPermissions().stream().map(Permission::getPermissionCode)
                .collect(Collectors.toCollection(HashSet::new));

        Set<String> shared = new HashSet<>(perms1);
        shared.retainAll(perms2);

        Set<String> onlyIn1 = new HashSet<>(perms1);
        onlyIn1.removeAll(perms2);

        Set<String> onlyIn2 = new HashSet<>(perms2);
        onlyIn2.removeAll(perms1);

        return new RoleCompareResponse(
                new RoleCompareResponse.RoleSummary(role1.getId(), role1.getRoleName(), perms1.size()),
                new RoleCompareResponse.RoleSummary(role2.getId(), role2.getRoleName(), perms2.size()),
                shared, onlyIn1, onlyIn2
        );
    }

    @Override
    public List<RoleResponseDto> listSystemRoles() {
        String tenantId = currentUser.getTenantId();
        return roleRepository.findAllByTenantIdAndType(tenantId, RoleType.SYSTEM)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RoleResponseDto cloneRole(String sourceRoleId, RoleCloneRequest request) {
        String tenantId = currentUser.getTenantId();

        // Tenant-scoped lookup only — a caller can never clone a role that
        // isn't visible in their own tenant, system role or otherwise.
        Role source = roleRepository.findByIdAndTenantId(sourceRoleId, tenantId)
                .orElseThrow(() -> new RoleNotFoundException(sourceRoleId));

        Role clone = new Role();
        clone.setTenantId(tenantId);
        clone.setRoleName(request.newName());
        clone.setDescription(source.getDescription());
        // Cloned roles are always CUSTOM, even when the source was SYSTEM —
        // and always get a fresh, unique code, never the source's code.
        clone.setRoleType(RoleType.CUSTOM);
        clone.setRoleCode(generateUniqueRoleCode(tenantId, request.newName()));
        clone.setClonedFromRoleId(source.getId());
        clone.setPermissions(new HashSet<>(source.getPermissions()));

        Role saved = roleRepository.save(clone);

        recordHistory(saved.getId(), "CREATED", "role",
                null, "Cloned from '" + source.getRoleName() + "'");

        return toDto(saved);
    }

    @Override
    public List<RoleHistoryDto> getHistory(String roleId) {
        String tenantId = currentUser.getTenantId();

        // Confirms the role belongs to the caller's tenant before returning
        // any history for it, even though history is looked up by roleId.
        roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        return roleHistoryRepository.findAllByRoleIdOrderByChangedAtDesc(roleId)
                .stream()
                .map(h -> new RoleHistoryDto(
                        h.getChangedByName(),
                        h.getChangedAt(),
                        h.getChangeType(),
                        h.getFieldName(),
                        h.getOldValue(),
                        h.getNewValue()
                ))
                .toList();
    }

    private void recordHistory(Long roleId, String changeType, String fieldName,
                               String oldValue, String newValue) {
        RoleHistory history = new RoleHistory();
        history.setRoleId(roleId);
        history.setChangedByUserId(currentUser.getUserId());
        history.setChangedByName(currentUser.getUserDisplayName());
        history.setChangedAt(Instant.now());
        history.setChangeType(changeType);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        roleHistoryRepository.save(history);
    }

    @Override
    public byte[] exportRoles(String format) {
        String tenantId = currentUser.getTenantId();
        // tenantId comes only from the verified token — the "format" query
        // param never influences which tenant's data gets pulled.
        List<Role> roles = roleRepository.findAllByTenantId(tenantId);
        return roleExportService.export(roles, format);
    }

    private String generateUniqueRoleCode(String tenantId, String baseName) {
        String base = baseName.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_");
        String candidate = base;
        int suffix = 1;
        while (roleRepository.existsByTenantIdAndRoleCode(tenantId, candidate)) {
            candidate = base + "_COPY_" + suffix++;
        }
        return candidate;
    }
}

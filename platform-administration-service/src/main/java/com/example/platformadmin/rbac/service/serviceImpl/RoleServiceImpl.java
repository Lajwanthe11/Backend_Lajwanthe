package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.common.abstracts.AbstractService;
import com.example.common.exception.BadRequestException;
import com.example.platformadmin.rbac.exception.ResourceNotFoundException;
import com.example.platformadmin.rbac.entity.RoleHistory;
import com.example.platformadmin.rbac.repository.*;
import com.example.platformadmin.rbac.exception.RoleNotFoundException;
import com.example.platformadmin.rbac.dto.request.*;
import com.example.platformadmin.rbac.dto.response.*;
import com.example.platformadmin.rbac.entity.Permission;
import com.example.platformadmin.rbac.entity.Role;
import com.example.platformadmin.rbac.entity.RoleTemplate;
import com.example.platformadmin.rbac.enums.RoleType;
import com.example.platformadmin.rbac.service.RoleExportService;
import com.example.platformadmin.rbac.service.RoleService;
import com.example.platformadmin.rbac.service.CurrentUserContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends AbstractService<Role, UUID, RoleRequestDto, RoleResponseDto>
                implements RoleService {

        private final RoleRepository roleRepository;
        private final RoleTemplateRepository roleTemplateRepository;
        private final CurrentUserContext currentUser;
        private final RoleHistoryRepository roleHistoryRepository;
        private final RoleExportService roleExportService;
        private final PermissionRepository permissionRepository;
        private final RolePermissionRepository rolePermissionRepository;

        public RoleServiceImpl(
                        RoleRepository roleRepository,
                        RoleTemplateRepository roleTemplateRepository,
                        CurrentUserContext currentUser,
                        RoleHistoryRepository roleHistoryRepository,
                        RoleExportService roleExportService,
                        PermissionRepository permissionRepository,
                        RolePermissionRepository rolePermissionRepository) {

                super(roleRepository, "Role");

                this.roleRepository = roleRepository;
                this.roleTemplateRepository = roleTemplateRepository;
                this.currentUser = currentUser;
                this.roleHistoryRepository = roleHistoryRepository;
                this.roleExportService = roleExportService;
                this.permissionRepository = permissionRepository;
                this.rolePermissionRepository = rolePermissionRepository;
        }

        // Convert current tenant ID from JWT String to UUID
        private UUID getCurrentTenantUuid() {
                return currentUser.getTenantId() != null ? currentUser.getTenantId() : null;
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
                role.setStatus(dto.getStatus() != null
                                ? dto.getStatus()
                                : "ACTIVE");

                role.setIsDeleted(false);

                if (dto.getTemplateId() != null) {
                        role.setCreatedFromTemplateId(dto.getTemplateId().toString());
                        if (dto.getPermissionCodes() == null || dto.getPermissionCodes().isEmpty()) {
                                roleTemplateRepository.findById(dto.getTemplateId()).ifPresent(template -> {
                                        Set<String> codes = template.getPermissions().stream()
                                                        .map(Permission::getPermissionCode)
                                                        .collect(Collectors.toSet());
                                        role.setPermissions(resolvePermissionsByCode(codes));
                                });
                        }
                }

                return role;
        }

        // Convert Role entity to response DTO
        @Override
        protected RoleResponseDto toDto(Role role) {
                Set<String> permissionCodes = role.getPermissions() != null
                                ? role.getPermissions().stream()
                                                .map(Permission::getPermissionCode)
                                                .collect(Collectors.toSet())
                                : Set.of();

                return new RoleResponseDto(
                                role.getId(),
                                role.getRoleName(),
                                role.getRoleCode(),
                                role.getRoleType(),
                                role.getDescription(),
                                role.getStatus(),
                                role.getIsDeleted(),
                                permissionCodes,
                                role.getCreatedAt(),
                                role.getCreatedBy(),
                                role.getUpdatedAt(),
                                role.getUpdatedBy());
        }

        // Update editable Role fields
        @Override
        protected void updateEntityFromDto(
                        Role role,
                        RoleRequestDto dto) {

                role.setRoleName(dto.getRoleName());
                role.setDescription(dto.getDescription());
        }

        // Check duplicate role details within current tenant
        @Override
        protected void beforeCreate(
                        Role role,
                        RoleRequestDto dto) {

                UUID tenantId = getCurrentTenantUuid();

                // BaseEntity is no longer used, so set tenant manually
                role.setTenantId(tenantId);

                if (roleRepository
                                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                                role.getRoleName(),
                                                tenantId)) {

                        throw new BadRequestException(
                                        "Role name already exists");
                }

                if (roleRepository
                                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                                role.getRoleCode(),
                                                tenantId)) {

                        throw new BadRequestException(
                                        "Role code already exists");
                }
        }

        // Search roles for current tenant
        @Override
        public List<RoleResponseDto> searchRoles(
                        String query,
                        RoleType roleType,
                        String status) {

                return roleRepository
                                .searchRoles(
                                                getCurrentTenantUuid(),
                                                query,
                                                roleType,
                                                status)
                                .stream()
                                .map(this::toDto)
                                .toList();
        }

        // Update role within current tenant
        @Override
        @Transactional
        public RoleResponseDto update(
                        UUID id,
                        RoleRequestDto dto) {

                validateId(id);

                UUID tenantId = getCurrentTenantUuid();

                Role role = roleRepository
                                .findByIdAndTenantIdAndIsDeletedFalse(
                                                id,
                                                tenantId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Role",
                                                "id",
                                                id));

                if (!role.getRoleName()
                                .equalsIgnoreCase(dto.getRoleName())
                                && roleRepository
                                                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                                                dto.getRoleName(),
                                                                tenantId)) {

                        throw new BadRequestException(
                                        "Role name already exists");
                }

                updateEntityFromDto(role, dto);

                return toDto(roleRepository.save(role));
        }

        // Soft delete role for current tenant
        @Override
        @Transactional
        public void deleteById(UUID id) {

                validateId(id);

                Role role = roleRepository
                                .findByIdAndTenantIdAndIsDeletedFalse(
                                                id,
                                                getCurrentTenantUuid())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Role",
                                                "id",
                                                id));

                if (isProtectedSystemRole(role)) {
                        throw new BadRequestException("System role cannot be deleted");
                }

                role.setIsDeleted(true);
                role.setDeletedAt(LocalDateTime.now());

                roleRepository.save(role);
        }

        // Activate or deactivate role
        @Override
        @Transactional
        public RoleResponseDto updateStatus(
                        UUID id,
                        String status) {

                validateId(id);

                if (status == null
                                || (!status.equalsIgnoreCase("ACTIVE")
                                                && !status.equalsIgnoreCase("INACTIVE"))) {

                        throw new BadRequestException(
                                        "Status must be ACTIVE or INACTIVE");
                }

                Role role = roleRepository
                                .findByIdAndTenantIdAndIsDeletedFalse(
                                                id,
                                                getCurrentTenantUuid())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Role",
                                                "id",
                                                id));

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
        public RoleResponseDto getById(UUID id) {

                validateId(id);

                Role role = roleRepository
                                .findByIdAndTenantIdAndIsDeletedFalse(
                                                id,
                                                getCurrentTenantUuid())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Role",
                                                "id",
                                                id));

                return toDto(role);
        }

        // Get all roles for current tenant
        @Override
        @Transactional(readOnly = true)
        public List<RoleResponseDto> getAll() {

                return roleRepository
                                .findByTenantIdAndIsDeletedFalse(
                                                getCurrentTenantUuid())
                                .stream()
                                .map(this::toDto)
                                .toList();
        }

        // Get paginated roles for current tenant
        @Override
        @Transactional(readOnly = true)
        public Page<RoleResponseDto> getAll(
                        Pageable pageable) {

                return roleRepository
                                .findByTenantIdAndIsDeletedFalse(
                                                getCurrentTenantUuid(),
                                                pageable)
                                .map(this::toDto);
        }

        // Check protected system roles
        private boolean isProtectedSystemRole(Role role) {

                String code = role.getRoleCode();

                return "SUPER_ADMIN".equalsIgnoreCase(code)
                                || "ADMIN".equalsIgnoreCase(code)
                                || "EMPLOYEE".equalsIgnoreCase(code);
        }

        // Get role count badges for current tenant
        @Override
        public Map<String, Long> getRoleCounts() {

                UUID tenantId = getCurrentTenantUuid();

                long totalRoles = roleRepository
                                .countByTenantIdAndIsDeletedFalse(
                                                tenantId);

                long systemRoles = roleRepository
                                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                                                tenantId,
                                                RoleType.SYSTEM);

                long customRoles = roleRepository
                                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                                                tenantId,
                                                RoleType.CUSTOM);

                return Map.of(
                                "totalRoles", totalRoles,
                                "systemRoles", systemRoles,
                                "customRoles", customRoles);
        }

        // Returns all templates for Super Admins, or just visible ones for everyone else, mapped to a summary DTO.
        @Override
        public List<RoleTemplateSummaryDto> listTemplates() {
                List<RoleTemplate> templates = currentUser.hasRole("SUPER_ADMIN")
                        ? roleTemplateRepository.findAllWithPermissions()
                        : roleTemplateRepository.findAllByHiddenFalse();

                return templates.stream()
                        .map(t -> new RoleTemplateSummaryDto(
                                t.getId(),
                                t.getName(),
                                t.getDescription(),
                                t.getPermissions().size(),
                                t.getRecommendedFor()))
                        .toList();
        }

        // Fetches one template by ID and returns its details plus permission codes.
        @Override
        @Transactional(readOnly = true)
        public RoleTemplateDetailDto getTemplateDetail(UUID templateId) {
                RoleTemplate template = roleTemplateRepository.findById(templateId)
                                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));

                Set<String> permissionCodes = template.getPermissions().stream()
                                .map(Permission::getPermissionCode)
                                .collect(Collectors.toSet());

                return new RoleTemplateDetailDto(
                                template.getId(), template.getName(), template.getDescription(),
                                template.getRecommendedFor(), permissionCodes);
        }

        // Toggles a template's hidden/shown flag without deleting it.
        @Override
        @Transactional
        public void updateTemplateVisibility(UUID templateId, boolean hidden) {
                RoleTemplate template = roleTemplateRepository.findById(templateId)
                                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
                template.setHidden(hidden);
                roleTemplateRepository.save(template);
        }

        // Returns all SYSTEM-type roles for the current tenant.
        @Override
        public List<RoleResponseDto> listSystemRoles() {
                UUID tenantId = getCurrentTenantUuid();
                return roleRepository.findAllByTenantIdAndType(tenantId, RoleType.SYSTEM)
                                .stream()
                                .map(this::toDto)
                                .toList();
        }

        // Clones a role's permissions into a new CUSTOM role with a unique code, and logs the action.
        @Override
        @Transactional
        public RoleResponseDto cloneRole(UUID sourceRoleId, RoleCloneRequest request) {
                UUID tenantId = getCurrentTenantUuid();
                Role source = roleRepository.findByIdAndTenantId(sourceRoleId, tenantId)
                                .orElseThrow(() -> new RoleNotFoundException(sourceRoleId));

                Role clone = new Role();
                clone.setTenantId(tenantId);
                clone.setRoleName(request.getNewName());
                clone.setDescription(source.getDescription());
                clone.setRoleType(RoleType.CUSTOM); // always CUSTOM, even cloning a SYSTEM role
                clone.setRoleCode(generateUniqueRoleCode(tenantId, request.getNewName()));
                clone.setClonedFromRoleId(source.getId());
                clone.setStatus("ACTIVE");

                Role saved = roleRepository.save(clone);

                for (Permission permission : source.getPermissions()) {
                        rolePermissionRepository.insertRolePermission(
                                saved.getId(),
                                permission.getPermissionId(),
                                tenantId,
                                "SYSTEM"
                        );
                        }

                recordHistory(saved.getId(), "CREATED", "role",
                                null, "Cloned from '" + source.getRoleName() + "'");

                return toDto(saved);
        }

        // Compares two roles' permissions and returns shared and unique permissions between them.
        @Override
        @Transactional(readOnly = true)
        public RoleCompareResponse compareRoles(UUID role1Id, UUID role2Id) {
                UUID tenantId = getCurrentTenantUuid();
                System.out.println(tenantId);
                Role role1 = roleRepository.findByIdAndTenantId(role1Id, tenantId)
                                .orElseThrow(() -> new RoleNotFoundException(role1Id));
                Role role2 = roleRepository.findByIdAndTenantId(role2Id, tenantId)
                                .orElseThrow(() -> new RoleNotFoundException(role2Id));

                Set<String> perms1 = role1.getPermissions().stream()
                                .map(Permission::getPermissionCode)
                                .collect(Collectors.toCollection(HashSet::new));
                Set<String> perms2 = role2.getPermissions().stream()
                                .map(Permission::getPermissionCode)
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
                                shared, onlyIn1, onlyIn2);
        }

        // Returns the audit history of changes for a given role.
        @Override
        public List<RoleHistoryDto> getHistory(UUID roleId) {
                UUID tenantId = getCurrentTenantUuid();

                // Confirms the role belongs to the caller's tenant before returning
                // any history for it.
                roleRepository.findByIdAndTenantId(roleId, tenantId)
                                .orElseThrow(() -> new RoleNotFoundException(roleId));

                return roleHistoryRepository.findAllByRoleIdOrderByChangedAtDesc(roleId)
                                .stream()
                                .map(h -> new RoleHistoryDto(
                                                h.getChangedByName(), h.getChangedAt(), h.getChangeType(),
                                                h.getFieldName(), h.getOldValue(), h.getNewValue()))
                                .toList();
        }

        // Exports all of the tenant's roles in the requested file format.
        @Override
        public byte[] exportRoles(String format) {
                UUID tenantId = getCurrentTenantUuid();
                // tenantId comes only from the verified token — "format" never
                // influences which tenant's data gets pulled.
                List<Role> roles = roleRepository.findAllByTenantId(tenantId);
                return roleExportService.export(roles, format);
        }

        // Converts permission code strings into actual Permission entities.
        private Set<Permission> resolvePermissionsByCode(Set<String> codes) {
                if (codes == null || codes.isEmpty())
                        return new HashSet<>();
                return new HashSet<>(permissionRepository.findByPermissionCodeIn(new ArrayList<>(codes)));
        }

        // Generates a unique, sanitized role code from a name.
        private String generateUniqueRoleCode(UUID tenantId, String baseName) {
                String base = baseName.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_");
                String candidate = base;
                int suffix = 1;
                while (roleRepository.existsByTenantIdAndRoleCode(tenantId, candidate)) {
                        candidate = base + "_COPY_" + suffix++;
                }
                return candidate;
        }

        //Saves one audit-log entry for a role change.
        private void recordHistory(UUID roleId, String changeType, String fieldName,
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
}
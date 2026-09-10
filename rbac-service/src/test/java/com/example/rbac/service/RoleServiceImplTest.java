package com.example.rbac.service;

import com.example.rbac.dto.RoleRequestDto;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.RoleTemplateRepository;
import com.example.rbac.service.serviceImpl.RoleServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

        @Mock
        private RoleRepository roleRepository;
        private RoleTemplateRepository roleTemplateRepository;
        private RoleServiceImpl roleService;
        private CurrentUserContext currentUserContext;

        @BeforeEach
        void setUp() {
                roleService = new RoleServiceImpl(roleRepository,roleTemplateRepository,currentUserContext);
        }

        @Test
        void createRole_success() {

                RoleRequestDto request = new RoleRequestDto();
                request.setRoleName("HR Manager");
                request.setRoleType(RoleType.CUSTOM);
                request.setDescription("HR role");
                request.setStatus("ACTIVE");

                // Duplicate role name does not exist
                when(roleRepository
                                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                                anyString(), any()))
                                .thenReturn(false);

                // Duplicate role code does not exist
                when(roleRepository
                                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                                anyString(), any()))
                                .thenReturn(false);

                Role savedRole = new Role();
                savedRole.setId(1L);
                savedRole.setRoleName("HR Manager");
                savedRole.setRoleCode("HR_MANAGER");
                savedRole.setRoleType(RoleType.CUSTOM);
                savedRole.setDescription("HR role");
                savedRole.setStatus("ACTIVE");
                savedRole.setIsDeleted(false);

                when(roleRepository.save(any(Role.class)))
                                .thenReturn(savedRole);

                RoleResponseDto response = roleService.create(request);

                assertNotNull(response);
                assertEquals("HR Manager", response.getRoleName());
                assertEquals("HR_MANAGER", response.getRoleCode());
                assertEquals(RoleType.CUSTOM, response.getRoleType());
                assertEquals("ACTIVE", response.getStatus());
        }

        @Test
        void updateStatus_success() {

                Role role = new Role();
                role.setId(1L);
                role.setRoleName("HR Manager");
                role.setRoleCode("HR_MANAGER");
                role.setRoleType(RoleType.CUSTOM);
                role.setStatus("ACTIVE");
                role.setIsDeleted(false);

                when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                                any(), any()))
                                .thenReturn(java.util.Optional.of(role));

                when(roleRepository.save(any(Role.class)))
                                .thenReturn(role);

                RoleResponseDto response = roleService.updateStatus(1L, "INACTIVE");

                assertEquals("INACTIVE", response.getStatus());
        }

        @Test
        void deleteRole_softDelete_success() {

                Role role = new Role();
                role.setId(1L);
                role.setRoleName("HR Manager");
                role.setRoleCode("HR_MANAGER");
                role.setRoleType(RoleType.CUSTOM);
                role.setStatus("ACTIVE");
                role.setIsDeleted(false);

                when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                                any(), any()))
                                .thenReturn(java.util.Optional.of(role));

                when(roleRepository.save(any(Role.class)))
                                .thenReturn(role);

                roleService.deleteById(1L);

                assertEquals(true, role.getIsDeleted());
        }

        @Test
        void getRoleById_success() {

                Role role = new Role();
                role.setId(1L);
                role.setRoleName("HR Manager");
                role.setRoleCode("HR_MANAGER");
                role.setRoleType(RoleType.CUSTOM);
                role.setStatus("ACTIVE");
                role.setIsDeleted(false);

                when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                                any(), any()))
                                .thenReturn(java.util.Optional.of(role));

                RoleResponseDto response = roleService.getById(1L);

                assertNotNull(response);
                assertEquals(1L, response.getId());
                assertEquals("HR Manager", response.getRoleName());
        }

        @Test
        void searchRoles_success() {

                Role role = new Role();
                role.setId(1L);
                role.setRoleName("HR Manager");
                role.setRoleCode("HR_MANAGER");
                role.setRoleType(RoleType.CUSTOM);
                role.setStatus("ACTIVE");
                role.setIsDeleted(false);

                when(roleRepository.searchRoles(
                                any(), any(), any(), any()))
                                .thenReturn(java.util.List.of(role));

                java.util.List<RoleResponseDto> result = roleService.searchRoles(
                                "HR",
                                RoleType.CUSTOM,
                                "ACTIVE");

                assertEquals(1, result.size());
                assertEquals("HR Manager", result.get(0).getRoleName());
        }

        @Test
        void getRoleCounts_success() {

                when(roleRepository.countByTenantIdAndIsDeletedFalse(any()))
                                .thenReturn(5L);

                when(roleRepository.countByTenantIdAndRoleTypeAndIsDeletedFalse(
                                any(), any()))
                                .thenReturn(2L, 3L);

                java.util.Map<String, Long> counts = roleService.getRoleCounts();

                assertEquals(5L, counts.get("totalRoles"));
                assertEquals(2L, counts.get("systemRoles"));
                assertEquals(3L, counts.get("customRoles"));
        }

        @Test
        void updateRole_success() {

                Role existingRole = new Role();
                existingRole.setId(1L);
                existingRole.setRoleName("HR Manager");
                existingRole.setRoleCode("HR_MANAGER");
                existingRole.setRoleType(RoleType.CUSTOM);
                existingRole.setDescription("Old description");
                existingRole.setStatus("ACTIVE");
                existingRole.setIsDeleted(false);

                RoleRequestDto request = new RoleRequestDto();
                request.setRoleName("HR Lead");
                request.setDescription("Updated description");

                when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                                any(), any()))
                                .thenReturn(java.util.Optional.of(existingRole));

                when(roleRepository
                                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                                anyString(), any()))
                                .thenReturn(false);

                when(roleRepository.save(any(Role.class)))
                                .thenReturn(existingRole);

                RoleResponseDto response = roleService.update(1L, request);

                assertEquals("HR Lead", response.getRoleName());
                assertEquals("Updated description", response.getDescription());

                // Code and type should not change during update
                assertEquals("HR_MANAGER", response.getRoleCode());
                assertEquals(RoleType.CUSTOM, response.getRoleType());
        }

        @Test
        void updateStatus_invalidStatus() {

                org.junit.jupiter.api.Assertions.assertThrows(
                                Exception.class,
                                () -> roleService.updateStatus(1L, "PENDING"));
        }

        @Test
        void getRoleById_notFound() {

                when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                                any(), any()))
                                .thenReturn(java.util.Optional.empty());

                org.junit.jupiter.api.Assertions.assertThrows(
                                Exception.class,
                                () -> roleService.getById(1L));
        }

        @Test
        void deleteProtectedSystemRole_shouldThrowException() {

                Role role = new Role();
                role.setId(1L);
                role.setRoleName("Admin");
                role.setRoleCode("ADMIN");
                role.setRoleType(RoleType.SYSTEM);
                role.setStatus("ACTIVE");
                role.setIsDeleted(false);

                when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                                any(), any()))
                                .thenReturn(java.util.Optional.of(role));

                org.junit.jupiter.api.Assertions.assertThrows(
                                Exception.class,
                                () -> roleService.deleteById(1L));
        }
}
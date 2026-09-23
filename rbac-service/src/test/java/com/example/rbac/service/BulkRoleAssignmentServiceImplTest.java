package com.example.rbac.service;

import com.example.rbac.dto.response.BulkOperationResponse;
import com.example.rbac.dto.request.BulkRoleAssignmentRequest;
import com.example.rbac.dto.request.BulkRoleRevokeRequest;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.UserRole;
import com.example.rbac.exception.RoleAssignmentValidationException;
import com.example.rbac.repository.UserRoleRepository;
import com.example.rbac.service.serviceImpl.BulkRoleAssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkRoleAssignmentServiceImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleLookupService roleLookupService;

    @Mock
    private RoleAuditService roleAuditService;

    @Mock
    private UserAssignmentSupportService userAssignmentSupportService;

    private BulkRoleAssignmentServiceImpl service;

    private UUID tenantId;
    private UUID actorId;
    private UUID roleId;
    private UUID userId;
    private Role role;

    @BeforeEach
    void setUp() {

        service = new BulkRoleAssignmentServiceImpl(
                userRoleRepository,
                roleLookupService,
                roleAuditService,
                userAssignmentSupportService,
                true
        );

        tenantId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        userId = UUID.randomUUID();

        role = new Role();
        role.setId(roleId);
        role.setTenantId(tenantId);
        role.setRoleCode("HR_MANAGER");
        role.setRoleName("HR Manager");
        role.isDeleted();
    }

    @Test
    void bulkAssign_shouldAssignRoleSuccessfully() {

        BulkRoleAssignmentRequest request =
                new BulkRoleAssignmentRequest(
                        List.of(userId),
                        roleId,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(30),
                        "Test assignment"
                );

        when(roleLookupService
                .getAssignableRole(tenantId, roleId))
                .thenReturn(role);

        when(userRoleRepository
                .existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId,
                        userId,
                        roleId
                ))
                .thenReturn(false);

        when(userRoleRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BulkOperationResponse response =
                service.bulkAssign(
                        tenantId,
                        actorId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                1,
                response.requestedCount()
        );

        assertEquals(
                1,
                response.successCount()
        );

        assertEquals(
                0,
                response.skippedCount()
        );

        assertEquals(
                0,
                response.failedCount()
        );

        assertEquals(
                "ASSIGNED",
                response.results().get(0).status()
        );

        assertTrue(
                response.results()
                        .get(0)
                        .message()
                        .contains("HR_MANAGER")
        );

        verify(userAssignmentSupportService)
                .validateAssignable(
                        tenantId,
                        userId
                );

        verify(userRoleRepository)
                .saveAll(argThat(iterable -> {

                    List<UserRole> captured =
                            new ArrayList<>();

                    iterable.forEach(captured::add);

                    if (captured.size() != 1) {
                        return false;
                    }

                    UserRole saved =
                            captured.get(0);

                    return tenantId.equals(
                            saved.getTenantId()
                    )
                            && userId.equals(
                            saved.getUserId()
                    )
                            && roleId.equals(
                            saved.getRoleId()
                    )
                            && actorId.equals(
                            saved.getAssignedBy()
                    )
                            && saved.isActive()
                            && !saved.isPrimary()
                            && saved.getAssignedAt() != null;
                }));

        verify(roleAuditService)
                .record(
                        tenantId,
                        actorId,
                        userId,
                        roleId,
                        "BULK_ROLE_ASSIGNED",
                        "Test assignment"
                );
    }

    @Test
    void bulkAssign_shouldSkipAlreadyAssignedRole() {

        BulkRoleAssignmentRequest request =
                new BulkRoleAssignmentRequest(
                        List.of(userId),
                        roleId,
                        LocalDate.now().plusDays(1),
                        null,
                        "Duplicate test"
                );

        when(roleLookupService
                .getAssignableRole(
                        tenantId,
                        roleId
                ))
                .thenReturn(role);

        when(userRoleRepository
                .existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId,
                        userId,
                        roleId
                ))
                .thenReturn(true);

        when(userRoleRepository.saveAll(anyList()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        BulkOperationResponse response =
                service.bulkAssign(
                        tenantId,
                        actorId,
                        request
                );

        assertEquals(
                0,
                response.successCount()
        );

        assertEquals(
                1,
                response.skippedCount()
        );

        assertEquals(
                "SKIPPED",
                response.results()
                        .get(0)
                        .status()
        );

        assertEquals(
                "already assigned",
                response.results()
                        .get(0)
                        .message()
        );

        verify(
                roleAuditService,
                never()
        ).record(
                any(),
                any(),
                any(),
                any(),
                anyString(),
                any()
        );
    }

    @Test
    void bulkAssign_shouldSkipDuplicateUserIdInsideSameRequest() {

        BulkRoleAssignmentRequest request =
                new BulkRoleAssignmentRequest(
                        List.of(
                                userId,
                                userId
                        ),
                        roleId,
                        LocalDate.now().plusDays(1),
                        null,
                        null
                );

        when(roleLookupService
                .getAssignableRole(
                        tenantId,
                        roleId
                ))
                .thenReturn(role);

        when(userRoleRepository
                .existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId,
                        userId,
                        roleId
                ))
                .thenReturn(false);

        when(userRoleRepository.saveAll(anyList()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        BulkOperationResponse response =
                service.bulkAssign(
                        tenantId,
                        actorId,
                        request
                );

        assertEquals(
                2,
                response.requestedCount()
        );

        assertEquals(
                1,
                response.successCount()
        );

        assertEquals(
                1,
                response.skippedCount()
        );

        assertTrue(
                response.results()
                        .stream()
                        .anyMatch(result ->
                                "SKIPPED".equals(
                                        result.status()
                                )
                                        && result.message()
                                        .contains(
                                                "Duplicate userId"
                                        )
                        )
        );
    }

    @Test
    void bulkAssign_shouldRejectPastEffectiveDate() {

        BulkRoleAssignmentRequest request =
                new BulkRoleAssignmentRequest(
                        List.of(userId),
                        roleId,
                        LocalDate.now().minusDays(1),
                        null,
                        null
                );

        RoleAssignmentValidationException exception =
                assertThrows(
                        RoleAssignmentValidationException.class,
                        () ->
                                service.bulkAssign(
                                        tenantId,
                                        actorId,
                                        request
                                )
                );

        assertEquals(
                "effectiveDate cannot be in the past",
                exception.getMessage()
        );

        verifyNoInteractions(
                roleLookupService
        );
    }

    @Test
    void bulkAssign_shouldRejectExpiryDateOnOrBeforeEffectiveDate() {

        LocalDate effectiveDate =
                LocalDate.now().plusDays(5);

        BulkRoleAssignmentRequest request =
                new BulkRoleAssignmentRequest(
                        List.of(userId),
                        roleId,
                        effectiveDate,
                        effectiveDate,
                        null
                );

        RoleAssignmentValidationException exception =
                assertThrows(
                        RoleAssignmentValidationException.class,
                        () ->
                                service.bulkAssign(
                                        tenantId,
                                        actorId,
                                        request
                                )
                );

        assertEquals(
                "expiryDate must be after effectiveDate",
                exception.getMessage()
        );
    }

    @Test
    void bulkAssign_shouldRejectMoreThan500Users() {

        List<UUID> users =
                IntStream.range(
                                0,
                                501
                        )
                        .mapToObj(i ->
                                UUID.randomUUID())
                        .toList();

        BulkRoleAssignmentRequest request =
                new BulkRoleAssignmentRequest(
                        users,
                        roleId,
                        LocalDate.now().plusDays(1),
                        null,
                        null
                );

        RoleAssignmentValidationException exception =
                assertThrows(
                        RoleAssignmentValidationException.class,
                        () ->
                                service.bulkAssign(
                                        tenantId,
                                        actorId,
                                        request
                                )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "maximum of 500"
                        )
        );
    }

    @Test
    void bulkAssign_shouldRejectMissingTenantContext() {

        BulkRoleAssignmentRequest request =
                new BulkRoleAssignmentRequest(
                        List.of(userId),
                        roleId,
                        LocalDate.now().plusDays(1),
                        null,
                        null
                );

        RoleAssignmentValidationException exception =
                assertThrows(
                        RoleAssignmentValidationException.class,
                        () ->
                                service.bulkAssign(
                                        null,
                                        actorId,
                                        request
                                )
                );

        assertEquals(
                "Tenant context is required",
                exception.getMessage()
        );
    }

    @Test
    void bulkAssign_shouldRejectMissingActorContext() {

        BulkRoleAssignmentRequest request =
                new BulkRoleAssignmentRequest(
                        List.of(userId),
                        roleId,
                        LocalDate.now().plusDays(1),
                        null,
                        null
                );

        RoleAssignmentValidationException exception =
                assertThrows(
                        RoleAssignmentValidationException.class,
                        () ->
                                service.bulkAssign(
                                        tenantId,
                                        null,
                                        request
                                )
                );

        assertEquals(
                "Authenticated actor context is required",
                exception.getMessage()
        );
    }

    @Test
    void bulkAssign_nonAtomicMode_shouldCollectPerUserFailureAndContinue() {

        BulkRoleAssignmentServiceImpl nonAtomicService =
                new BulkRoleAssignmentServiceImpl(
                        userRoleRepository,
                        roleLookupService,
                        roleAuditService,
                        userAssignmentSupportService,
                        false
                );

        UUID failingUser =
                UUID.randomUUID();

        UUID validUser =
                UUID.randomUUID();

        BulkRoleAssignmentRequest request =
                new BulkRoleAssignmentRequest(
                        List.of(
                                failingUser,
                                validUser
                        ),
                        roleId,
                        LocalDate.now().plusDays(1),
                        null,
                        null
                );

        when(roleLookupService
                .getAssignableRole(
                        tenantId,
                        roleId
                ))
                .thenReturn(role);

        doThrow(
                new RoleAssignmentValidationException(
                        "User is inactive"
                )
        )
                .when(
                        userAssignmentSupportService
                )
                .validateAssignable(
                        tenantId,
                        failingUser
                );

        when(userRoleRepository
                .existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId,
                        validUser,
                        roleId
                ))
                .thenReturn(false);

        when(userRoleRepository
                .saveAll(anyList()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        BulkOperationResponse response =
                nonAtomicService.bulkAssign(
                        tenantId,
                        actorId,
                        request
                );

        assertEquals(
                1,
                response.successCount()
        );

        assertEquals(
                1,
                response.failedCount()
        );

        assertTrue(
                response.results()
                        .stream()
                        .anyMatch(result ->
                                result.userId()
                                        .equals(failingUser)
                                        && "FAILED".equals(
                                        result.status()
                                )
                        )
        );

        assertTrue(
                response.results()
                        .stream()
                        .anyMatch(result ->
                                result.userId()
                                        .equals(validUser)
                                        && "ASSIGNED".equals(
                                        result.status()
                                )
                        )
        );
    }

    @Test
    void bulkRevoke_shouldRevokeRoleSuccessfully() {

        UserRole assignment =
                activeAssignment(
                        userId,
                        false,
                        LocalDate.now()
                );

        BulkRoleRevokeRequest request =
                new BulkRoleRevokeRequest(
                        List.of(userId),
                        roleId,
                        "Access no longer required"
                );

        when(roleLookupService
                .getRole(
                        tenantId,
                        roleId
                ))
                .thenReturn(role);

        when(userRoleRepository
                .findFirstByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId,
                        userId,
                        roleId
                ))
                .thenReturn(
                        Optional.of(
                                assignment
                        )
                );

        when(userRoleRepository
                .countCurrentActiveAssignmentsForUser(
                        eq(tenantId),
                        eq(userId),
                        any(LocalDate.class)
                ))
                .thenReturn(2L);

        when(userRoleRepository.saveAll(anyList()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        BulkOperationResponse response =
                service.bulkRevoke(
                        tenantId,
                        actorId,
                        request
                );

        assertEquals(
                1,
                response.successCount()
        );

        assertEquals(
                "REVOKED",
                response.results()
                        .get(0)
                        .status()
        );

        assertFalse(
                assignment.isActive()
        );

        assertEquals(
                actorId,
                assignment.getRevokedBy()
        );

        assertEquals(
                "Access no longer required",
                assignment.getRevokeReason()
        );

        assertNotNull(
                assignment.getRevokedAt()
        );

        verify(roleAuditService)
                .record(
                        tenantId,
                        actorId,
                        userId,
                        roleId,
                        "BULK_ROLE_REVOKED",
                        "Access no longer required"
                );
    }

    @Test
    void bulkRevoke_shouldSkipWhenRoleIsNotAssigned() {

        BulkRoleRevokeRequest request =
                new BulkRoleRevokeRequest(
                        List.of(userId),
                        roleId,
                        "Cleanup"
                );

        when(roleLookupService
                .getRole(
                        tenantId,
                        roleId
                ))
                .thenReturn(role);

        when(userRoleRepository
                .findFirstByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId,
                        userId,
                        roleId
                ))
                .thenReturn(
                        Optional.empty()
                );

        when(userRoleRepository
                .saveAll(anyList()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        BulkOperationResponse response =
                service.bulkRevoke(
                        tenantId,
                        actorId,
                        request
                );

        assertEquals(
                0,
                response.successCount()
        );

        assertEquals(
                1,
                response.skippedCount()
        );

        assertEquals(
                "Role is not currently assigned",
                response.results()
                        .get(0)
                        .message()
        );
    }

    @Test
    void bulkRevoke_shouldRejectPrimaryRole() {

        UserRole assignment =
                activeAssignment(
                        userId,
                        true,
                        LocalDate.now()
                );

        BulkRoleRevokeRequest request =
                new BulkRoleRevokeRequest(
                        List.of(userId),
                        roleId,
                        "Trying to revoke primary"
                );

        when(roleLookupService
                .getRole(
                        tenantId,
                        roleId
                ))
                .thenReturn(role);

        when(userRoleRepository
                .findFirstByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId,
                        userId,
                        roleId
                ))
                .thenReturn(
                        Optional.of(
                                assignment
                        )
                );

        RoleAssignmentValidationException exception =
                assertThrows(
                        RoleAssignmentValidationException.class,
                        () ->
                                service.bulkRevoke(
                                        tenantId,
                                        actorId,
                                        request
                                )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Cannot revoke a primary role"
                        )
        );

        assertTrue(
                assignment.isActive()
        );
    }

    @Test
    void bulkRevoke_shouldRejectLastCurrentActiveRole() {

        UserRole assignment =
                activeAssignment(
                        userId,
                        false,
                        LocalDate.now()
                );

        BulkRoleRevokeRequest request =
                new BulkRoleRevokeRequest(
                        List.of(userId),
                        roleId,
                        "Last role test"
                );

        when(roleLookupService
                .getRole(
                        tenantId,
                        roleId
                ))
                .thenReturn(role);

        when(userRoleRepository
                .findFirstByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                        tenantId,
                        userId,
                        roleId
                ))
                .thenReturn(
                        Optional.of(
                                assignment
                        )
                );

        when(userRoleRepository
                .countCurrentActiveAssignmentsForUser(
                        eq(tenantId),
                        eq(userId),
                        any(LocalDate.class)
                ))
                .thenReturn(1L);

        RoleAssignmentValidationException exception =
                assertThrows(
                        RoleAssignmentValidationException.class,
                        () ->
                                service.bulkRevoke(
                                        tenantId,
                                        actorId,
                                        request
                                )
                );

        assertEquals(
                "A user must always have at least one active role",
                exception.getMessage()
        );

        assertTrue(
                assignment.isActive()
        );
    }

    private UserRole activeAssignment(
            UUID user,
            boolean primary,
            LocalDate effectiveDate
    ) {

        return UserRole.builder()
                .userRoleId(
                        UUID.randomUUID()
                )
                .tenantId(
                        tenantId
                )
                .userId(
                        user
                )
                .roleId(
                        roleId
                )
                .primary(
                        primary
                )
                .effectiveDate(
                        effectiveDate
                )
                .assignedBy(
                        actorId
                )
                .assignedAt(
                        LocalDateTime.now()
                )
                .active(
                        true
                )
                .build();
    }
}
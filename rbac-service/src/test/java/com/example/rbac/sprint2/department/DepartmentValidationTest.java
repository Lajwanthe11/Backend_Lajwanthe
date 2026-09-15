package com.example.qa.sprint2.department;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.common.exception.BadRequestException;
import com.example.rbac.entity.RoleDepartmentMap;
import com.example.rbac.repository.RoleDepartmentMapRepository;
import com.example.rbac.service.serviceImpl.DepartmentPermissionServiceImpl;

@ExtendWith(MockitoExtension.class)
class DepartmentValidationTest {

    @Mock
    private RoleDepartmentMapRepository repository;

    @InjectMocks
    private DepartmentPermissionServiceImpl service;

    // =========================================================
    // NULL
    // =========================================================

    @Test
    void nullDepartmentList_shouldBeRejected() {

        UUID roleId = UUID.randomUUID();

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> service.updateDepartmentScope(
                                roleId,
                                null
                        )
                );

        assertEquals(
                "Department list cannot be empty",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    // =========================================================
    // EMPTY
    // =========================================================

    @Test
    void emptyDepartmentList_shouldBeRejected() {

        UUID roleId = UUID.randomUUID();

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> service.updateDepartmentScope(
                                roleId,
                                List.of()
                        )
                );

        assertEquals(
                "Department list cannot be empty",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    // =========================================================
    // VALID
    // =========================================================

    @Test
    void nonEmptyDepartmentList_shouldBeAccepted() {

        UUID roleId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        service.updateDepartmentScope(
                roleId,
                List.of(departmentId)
        );

        verify(repository)
                .deleteByUserRoleId(roleId);

        verify(repository)
                .save(any(RoleDepartmentMap.class));
    }

    @Test
    void multipleDepartments_shouldBeAccepted() {

        UUID roleId = UUID.randomUUID();

        UUID department1 = UUID.randomUUID();
        UUID department2 = UUID.randomUUID();
        UUID department3 = UUID.randomUUID();

        service.updateDepartmentScope(
                roleId,
                List.of(
                        department1,
                        department2,
                        department3
                )
        );

        verify(repository)
                .deleteByUserRoleId(roleId);

        verify(
                repository,
                org.mockito.Mockito.times(3)
        )
        .save(any(RoleDepartmentMap.class));
    }

    // =========================================================
    // VALIDATION MUST HAPPEN BEFORE DATABASE OPERATIONS
    // =========================================================

    @Test
    void nullDepartmentList_shouldNotDeleteOrSave() {

        UUID roleId = UUID.randomUUID();

        assertThrows(
                BadRequestException.class,
                () -> service.updateDepartmentScope(
                        roleId,
                        null
                )
        );

        verify(
                repository,
                never()
        )
        .deleteByUserRoleId(roleId);

        verify(
                repository,
                never()
        )
        .save(any(RoleDepartmentMap.class));
    }

    @Test
    void emptyDepartmentList_shouldNotDeleteOrSave() {

        UUID roleId = UUID.randomUUID();

        assertThrows(
                BadRequestException.class,
                () -> service.updateDepartmentScope(
                        roleId,
                        List.of()
                )
        );

        verify(
                repository,
                never()
        )
        .deleteByUserRoleId(roleId);

        verify(
                repository,
                never()
        )
        .save(any(RoleDepartmentMap.class));
    }
}
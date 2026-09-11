package com.example.qa.sprint2.department;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.common.exception.BadRequestException;
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

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> service.updateDepartmentScope(
                                1L,
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

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> service.updateDepartmentScope(
                                1L,
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

        service.updateDepartmentScope(
                1L,
                List.of(101L)
        );

        verify(repository)
                .deleteByUserRoleId(1L);

        verify(repository)
                .save(any());
    }

    @Test
    void multipleDepartments_shouldBeAccepted() {

        service.updateDepartmentScope(
                1L,
                List.of(101L, 102L, 103L)
        );

        verify(repository)
                .deleteByUserRoleId(1L);

        verify(repository, times(3))
                .save(any());
    }
}
package com.example.qa.sprint2.department;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.common.exception.BadRequestException;
import com.example.rbac.entity.RoleDepartmentMap;
import com.example.rbac.repository.RoleDepartmentMapRepository;
import com.example.rbac.service.serviceImpl.DepartmentPermissionServiceImpl;

@ExtendWith(MockitoExtension.class)
class DepartmentScopeTest {

    @Mock
    private RoleDepartmentMapRepository repository;

    @InjectMocks
    private DepartmentPermissionServiceImpl service;

    // =========================================================
    // GET
    // =========================================================

    @Test
    void shouldReturnExistingDepartmentScope() {

        RoleDepartmentMap map1 =
                createMap(1L, 101L);

        RoleDepartmentMap map2 =
                createMap(1L, 102L);

        when(repository.findByUserRoleId(1L))
                .thenReturn(List.of(map1, map2));

        List<Long> result =
                service.getDepartmentScope(1L);

        assertEquals(
                List.of(101L, 102L),
                result
        );

        verify(repository)
                .findByUserRoleId(1L);
    }

    @Test
    void shouldReturnEmptyScopeWhenNoMappingsExist() {

        when(repository.findByUserRoleId(1L))
                .thenReturn(List.of());

        List<Long> result =
                service.getDepartmentScope(1L);

        assertTrue(result.isEmpty());

        verify(repository)
                .findByUserRoleId(1L);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void shouldDeleteOldScopeBeforeSavingNewScope() {

        Long roleId = 1L;

        List<Long> newDepartments =
                List.of(101L, 102L);

        service.updateDepartmentScope(
                roleId,
                newDepartments
        );

        InOrder inOrder =
                inOrder(repository);

        inOrder.verify(repository)
                .deleteByUserRoleId(roleId);

        /*
         * Two department IDs means two save() calls.
         */
        inOrder.verify(
                repository,
                times(2)
        ).save(any(RoleDepartmentMap.class));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldReplaceOldScopeWithNewDepartmentMappings() {

        Long roleId = 10L;

        List<Long> newDepartments =
                List.of(201L, 202L, 203L);

        service.updateDepartmentScope(
                roleId,
                newDepartments
        );

        verify(repository)
                .deleteByUserRoleId(roleId);

        ArgumentCaptor<RoleDepartmentMap> captor =
                ArgumentCaptor.forClass(
                        RoleDepartmentMap.class
                );

        verify(repository, times(3))
                .save(captor.capture());

        List<RoleDepartmentMap> mappings =
                captor.getAllValues();

        assertEquals(3, mappings.size());

        assertEquals(
                roleId,
                mappings.get(0).getUserRoleId()
        );

        assertEquals(
                201L,
                mappings.get(0).getDepartmentId()
        );

        assertEquals(
                202L,
                mappings.get(1).getDepartmentId()
        );

        assertEquals(
                203L,
                mappings.get(2).getDepartmentId()
        );
    }

    @Test
    void shouldCreateExactlyOneMappingForSingleDepartment() {

        service.updateDepartmentScope(
                1L,
                List.of(999L)
        );

        verify(repository)
                .deleteByUserRoleId(1L);

        verify(repository, times(1))
                .save(any(RoleDepartmentMap.class));
    }

    // =========================================================
    // INVALID INPUT
    // =========================================================

    @Test
    void shouldRejectNullDepartmentList() {

        assertThrows(
                BadRequestException.class,
                () -> service.updateDepartmentScope(
                        1L,
                        null
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectEmptyDepartmentList() {

        assertThrows(
                BadRequestException.class,
                () -> service.updateDepartmentScope(
                        1L,
                        List.of()
                )
        );

        verifyNoInteractions(repository);
    }

    // =========================================================
    // DELETE ALL
    // =========================================================

    @Test
    void shouldRemoveAllDepartmentScope() {

        service.removeAllDepartmentScope(1L);

        verify(repository)
                .deleteByUserRoleId(1L);

        verify(repository, never())
                .save(any(RoleDepartmentMap.class));
    }

    // =========================================================
    // HELPER
    // =========================================================

    private RoleDepartmentMap createMap(
            Long userRoleId,
            Long departmentId) {

        RoleDepartmentMap map =
                new RoleDepartmentMap();

        map.setUserRoleId(userRoleId);
        map.setDepartmentId(departmentId);

        return map;
    }
}
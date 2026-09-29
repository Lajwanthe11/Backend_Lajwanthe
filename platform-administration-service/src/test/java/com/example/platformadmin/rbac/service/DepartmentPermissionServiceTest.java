package com.example.platformadmin.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.common.exception.BadRequestException;
import com.example.platformadmin.rbac.entity.RoleDepartmentMap;
import com.example.platformadmin.rbac.repository.RoleDepartmentMapRepository;
import com.example.platformadmin.rbac.service.serviceImpl.DepartmentPermissionServiceImpl;

@ExtendWith(MockitoExtension.class)
class DepartmentPermissionServiceTest {

    @Mock
    private RoleDepartmentMapRepository repository;

    @InjectMocks
    private DepartmentPermissionServiceImpl service;

    // =========================================================
    // GET
    // =========================================================

    @Test
    void getDepartmentScope_shouldReturnDepartmentIds() {

        UUID roleId = UUID.randomUUID();

        UUID department1 = UUID.randomUUID();
        UUID department2 = UUID.randomUUID();
        UUID department3 = UUID.randomUUID();

        RoleDepartmentMap map1 =
                createMap(roleId, department1);

        RoleDepartmentMap map2 =
                createMap(roleId, department2);

        RoleDepartmentMap map3 =
                createMap(roleId, department3);

        when(repository.findByUserRoleId(roleId))
                .thenReturn(List.of(map1, map2, map3));

        List<UUID> result =
                service.getDepartmentScope(roleId);

        assertNotNull(result);

        assertEquals(
                List.of(
                        department1,
                        department2,
                        department3
                ),
                result
        );

        verify(repository)
                .findByUserRoleId(roleId);
    }

    @Test
    void getDepartmentScope_shouldReturnEmptyListWhenNoScopeExists() {

        UUID roleId = UUID.randomUUID();

        when(repository.findByUserRoleId(roleId))
                .thenReturn(List.of());

        List<UUID> result =
                service.getDepartmentScope(roleId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repository)
                .findByUserRoleId(roleId);
    }

    @Test
    void getDepartmentScope_shouldUseCorrectRoleId() {

        UUID roleId = UUID.randomUUID();
        UUID otherRoleId = UUID.randomUUID();

        when(repository.findByUserRoleId(roleId))
                .thenReturn(List.of());

        service.getDepartmentScope(roleId);

        verify(repository)
                .findByUserRoleId(roleId);

        verify(repository, never())
                .findByUserRoleId(otherRoleId);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void updateDepartmentScope_shouldDeleteOldScopeBeforeSavingNewScope() {

        UUID roleId = UUID.randomUUID();

        UUID department1 = UUID.randomUUID();
        UUID department2 = UUID.randomUUID();
        UUID department3 = UUID.randomUUID();

        List<UUID> departmentIds =
                List.of(
                        department1,
                        department2,
                        department3
                );

        service.updateDepartmentScope(
                roleId,
                departmentIds
        );

        InOrder inOrder =
                inOrder(repository);

        inOrder.verify(repository)
                .deleteByUserRoleId(roleId);

        inOrder.verify(
                repository,
                times(3)
        )
        .save(any(RoleDepartmentMap.class));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void updateDepartmentScope_shouldSaveAllDepartmentIds() {

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

        ArgumentCaptor<RoleDepartmentMap> captor =
                ArgumentCaptor.forClass(
                        RoleDepartmentMap.class
                );

        verify(repository, times(3))
                .save(captor.capture());

        List<RoleDepartmentMap> savedMaps =
                captor.getAllValues();

        assertEquals(3, savedMaps.size());

        assertEquals(
                roleId,
                savedMaps.get(0).getUserRoleId()
        );

        assertEquals(
                department1,
                savedMaps.get(0).getDepartmentId()
        );

        assertEquals(
                roleId,
                savedMaps.get(1).getUserRoleId()
        );

        assertEquals(
                department2,
                savedMaps.get(1).getDepartmentId()
        );

        assertEquals(
                roleId,
                savedMaps.get(2).getUserRoleId()
        );

        assertEquals(
                department3,
                savedMaps.get(2).getDepartmentId()
        );
    }

    @Test
    void updateDepartmentScope_shouldSaveOneDepartment() {

        UUID roleId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        service.updateDepartmentScope(
                roleId,
                List.of(departmentId)
        );

        verify(repository)
                .deleteByUserRoleId(roleId);

        ArgumentCaptor<RoleDepartmentMap> captor =
                ArgumentCaptor.forClass(
                        RoleDepartmentMap.class
                );

        verify(repository)
                .save(captor.capture());

        RoleDepartmentMap saved =
                captor.getValue();

        assertEquals(
                roleId,
                saved.getUserRoleId()
        );

        assertEquals(
                departmentId,
                saved.getDepartmentId()
        );
    }

    @Test
    void updateDepartmentScope_shouldCreateSeparateMapForEachDepartment() {

        UUID roleId = UUID.randomUUID();

        UUID department1 = UUID.randomUUID();
        UUID department2 = UUID.randomUUID();
        UUID department3 = UUID.randomUUID();
        UUID department4 = UUID.randomUUID();

        service.updateDepartmentScope(
                roleId,
                List.of(
                        department1,
                        department2,
                        department3,
                        department4
                )
        );

        ArgumentCaptor<RoleDepartmentMap> captor =
                ArgumentCaptor.forClass(
                        RoleDepartmentMap.class
                );

        verify(repository, times(4))
                .save(captor.capture());

        List<RoleDepartmentMap> maps =
                captor.getAllValues();

        assertEquals(
                List.of(
                        department1,
                        department2,
                        department3,
                        department4
                ),
                maps.stream()
                        .map(RoleDepartmentMap::getDepartmentId)
                        .toList()
        );

        assertTrue(
                maps.stream()
                        .allMatch(map ->
                                roleId.equals(
                                        map.getUserRoleId()
                                ))
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    @Test
    void updateDepartmentScope_shouldRejectNullDepartmentList() {

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

        verify(repository, never())
                .deleteByUserRoleId(roleId);

        verify(repository, never())
                .save(any(RoleDepartmentMap.class));
    }

    @Test
    void updateDepartmentScope_shouldRejectEmptyDepartmentList() {

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

        verify(repository, never())
                .deleteByUserRoleId(roleId);

        verify(repository, never())
                .save(any(RoleDepartmentMap.class));
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void removeAllDepartmentScope_shouldDeleteScope() {

        UUID roleId = UUID.randomUUID();

        service.removeAllDepartmentScope(roleId);

        verify(repository)
                .deleteByUserRoleId(roleId);
    }

    @Test
    void removeAllDepartmentScope_shouldUseCorrectRoleId() {

        UUID roleId = UUID.randomUUID();
        UUID otherRoleId = UUID.randomUUID();

        service.removeAllDepartmentScope(roleId);

        verify(repository)
                .deleteByUserRoleId(roleId);

        verify(repository, never())
                .deleteByUserRoleId(otherRoleId);
    }

    @Test
    void removeAllDepartmentScope_shouldNotSaveAnything() {

        UUID roleId = UUID.randomUUID();

        service.removeAllDepartmentScope(roleId);

        verify(repository, never())
                .save(any(RoleDepartmentMap.class));
    }

    // =========================================================
    // HELPER
    // =========================================================

    private RoleDepartmentMap createMap(
            UUID userRoleId,
            UUID departmentId) {

        RoleDepartmentMap map =
                new RoleDepartmentMap();

        map.setUserRoleId(userRoleId);
        map.setDepartmentId(departmentId);

        return map;
    }
}
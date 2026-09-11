package com.example.qa.sprint2.department;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class DepartmentPermissionServiceTest {

    @Mock
    private RoleDepartmentMapRepository repository;

    @InjectMocks
    private DepartmentPermissionServiceImpl service;

    // =========================================================
    // GET DEPARTMENT SCOPE
    // =========================================================

    @Test
    void getDepartmentScope_shouldReturnDepartmentIds() {

        RoleDepartmentMap map1 = createMap(100L, 10L);
        RoleDepartmentMap map2 = createMap(100L, 20L);
        RoleDepartmentMap map3 = createMap(100L, 30L);

        when(repository.findByUserRoleId(100L))
                .thenReturn(List.of(map1, map2, map3));

        List<Long> result =
                service.getDepartmentScope(100L);

        assertNotNull(result);
        assertEquals(
                List.of(10L, 20L, 30L),
                result
        );

        verify(repository)
                .findByUserRoleId(100L);
    }

    @Test
    void getDepartmentScope_shouldReturnEmptyListWhenNoScopeExists() {

        when(repository.findByUserRoleId(100L))
                .thenReturn(List.of());

        List<Long> result =
                service.getDepartmentScope(100L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repository)
                .findByUserRoleId(100L);
    }

    @Test
    void getDepartmentScope_shouldUseCorrectUserRoleId() {

        when(repository.findByUserRoleId(200L))
                .thenReturn(List.of());

        service.getDepartmentScope(200L);

        verify(repository)
                .findByUserRoleId(200L);

        verify(repository, never())
                .findByUserRoleId(100L);
    }

    // =========================================================
    // UPDATE DEPARTMENT SCOPE
    // =========================================================

    @Test
    void updateDepartmentScope_shouldDeleteOldScopeBeforeSavingNewScope() {

        List<Long> departmentIds =
                List.of(10L, 20L, 30L);

        service.updateDepartmentScope(
                100L,
                departmentIds
        );

        InOrder inOrder =
                inOrder(repository);

        inOrder.verify(repository)
                .deleteByUserRoleId(100L);

        inOrder.verify(repository, times(3))
                .save(any(RoleDepartmentMap.class));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void updateDepartmentScope_shouldSaveAllDepartmentIds() {

        List<Long> departmentIds =
                List.of(10L, 20L, 30L);

        service.updateDepartmentScope(
                100L,
                departmentIds
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
                100L,
                savedMaps.get(0).getUserRoleId()
        );

        assertEquals(
                10L,
                savedMaps.get(0).getDepartmentId()
        );

        assertEquals(
                100L,
                savedMaps.get(1).getUserRoleId()
        );

        assertEquals(
                20L,
                savedMaps.get(1).getDepartmentId()
        );

        assertEquals(
                100L,
                savedMaps.get(2).getUserRoleId()
        );

        assertEquals(
                30L,
                savedMaps.get(2).getDepartmentId()
        );
    }

    @Test
    void updateDepartmentScope_shouldDeleteOldScope() {

        service.updateDepartmentScope(
                100L,
                List.of(50L)
        );

        verify(repository)
                .deleteByUserRoleId(100L);
    }

    @Test
    void updateDepartmentScope_shouldSaveOneDepartment() {

        service.updateDepartmentScope(
                100L,
                List.of(50L)
        );

        ArgumentCaptor<RoleDepartmentMap> captor =
                ArgumentCaptor.forClass(
                        RoleDepartmentMap.class
                );

        verify(repository)
                .save(captor.capture());

        RoleDepartmentMap saved =
                captor.getValue();

        assertEquals(
                100L,
                saved.getUserRoleId()
        );

        assertEquals(
                50L,
                saved.getDepartmentId()
        );
    }

    @Test
    void updateDepartmentScope_shouldRejectNullDepartmentList() {

        assertThrows(
                BadRequestException.class,
                () -> service.updateDepartmentScope(
                        100L,
                        null
                )
        );

        verify(repository, never())
                .deleteByUserRoleId(any());

        verify(repository, never())
                .save(any(RoleDepartmentMap.class));
    }

    @Test
    void updateDepartmentScope_shouldRejectEmptyDepartmentList() {

        assertThrows(
                BadRequestException.class,
                () -> service.updateDepartmentScope(
                        100L,
                        List.of()
                )
        );

        verify(repository, never())
                .deleteByUserRoleId(any());

        verify(repository, never())
                .save(any(RoleDepartmentMap.class));
    }

    @Test
    void updateDepartmentScope_shouldUseCorrectUserRoleIdForDelete() {

        service.updateDepartmentScope(
                999L,
                List.of(10L, 20L)
        );

        verify(repository)
                .deleteByUserRoleId(999L);

        verify(repository, never())
                .deleteByUserRoleId(100L);
    }

    @Test
    void updateDepartmentScope_shouldCreateSeparateMapForEachDepartment() {

        service.updateDepartmentScope(
                100L,
                List.of(10L, 20L, 30L, 40L)
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
                List.of(10L, 20L, 30L, 40L),
                maps.stream()
                        .map(RoleDepartmentMap::getDepartmentId)
                        .toList()
        );

        assertTrue(
                maps.stream()
                        .allMatch(map ->
                                Long.valueOf(100L)
                                        .equals(map.getUserRoleId()))
        );
    }

    // =========================================================
    // REMOVE ALL DEPARTMENT SCOPE
    // =========================================================

    @Test
    void removeAllDepartmentScope_shouldDeleteScope() {

        service.removeAllDepartmentScope(100L);

        verify(repository)
                .deleteByUserRoleId(100L);
    }

    @Test
    void removeAllDepartmentScope_shouldUseCorrectUserRoleId() {

        service.removeAllDepartmentScope(999L);

        verify(repository)
                .deleteByUserRoleId(999L);

        verify(repository, never())
                .deleteByUserRoleId(100L);
    }

    @Test
    void removeAllDepartmentScope_shouldNotSaveAnything() {

        service.removeAllDepartmentScope(100L);

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
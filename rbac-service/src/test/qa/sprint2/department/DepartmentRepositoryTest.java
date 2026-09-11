package com.example.qa.sprint2.department;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.rbac.RbacApplication;
import org.springframework.test.context.ContextConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.rbac.entity.RoleDepartmentMap;
import com.example.rbac.repository.RoleDepartmentMapRepository;

@DataJpaTest
class DepartmentRepositoryTest {

    @Autowired
    private RoleDepartmentMapRepository repository;

    @BeforeEach
    void setUp() {

        repository.deleteAll();

        repository.save(
                createMap(1L, 101L)
        );

        repository.save(
                createMap(1L, 102L)
        );

        repository.save(
                createMap(1L, 103L)
        );

        repository.save(
                createMap(2L, 201L)
        );
    }

    // =========================================================
    // FIND
    // =========================================================

    @Test
    void findByUserRoleId_shouldReturnMappingsForRole() {

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(1L);

        assertEquals(3, result.size());

        assertTrue(
                result.stream()
                        .allMatch(map ->
                                map.getUserRoleId()
                                        .equals(1L))
        );
    }

    @Test
    void findByUserRoleId_shouldNotReturnOtherRoleMappings() {

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(1L);

        assertTrue(
                result.stream()
                        .noneMatch(map ->
                                map.getUserRoleId()
                                        .equals(2L))
        );
    }

    @Test
    void findByUserRoleId_shouldReturnEmptyWhenNoMappingsExist() {

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserRoleId_shouldReturnCorrectDepartmentIds() {

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(1L);

        List<Long> departmentIds =
                result.stream()
                        .map(RoleDepartmentMap::getDepartmentId)
                        .toList();

        assertTrue(
                departmentIds.contains(101L)
        );

        assertTrue(
                departmentIds.contains(102L)
        );

        assertTrue(
                departmentIds.contains(103L)
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void deleteByUserRoleId_shouldDeleteOnlySelectedRoleMappings() {

        repository.deleteByUserRoleId(1L);

        List<RoleDepartmentMap> roleOne =
                repository.findByUserRoleId(1L);

        List<RoleDepartmentMap> roleTwo =
                repository.findByUserRoleId(2L);

        assertTrue(roleOne.isEmpty());

        assertEquals(1, roleTwo.size());

        assertEquals(
                201L,
                roleTwo.get(0).getDepartmentId()
        );
    }

    @Test
    void deleteByUserRoleId_shouldDoNothingForUnknownRole() {

        repository.deleteByUserRoleId(999L);

        assertEquals(
                3,
                repository.findByUserRoleId(1L).size()
        );

        assertEquals(
                1,
                repository.findByUserRoleId(2L).size()
        );
    }

    // =========================================================
    // SAVE
    // =========================================================

    @Test
    void save_shouldPersistMapping() {

        RoleDepartmentMap map =
                createMap(3L, 301L);

        RoleDepartmentMap saved =
                repository.save(map);

        assertNotNull(saved.getId());

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(3L);

        assertEquals(1, result.size());

        assertEquals(
                301L,
                result.get(0).getDepartmentId()
        );
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
package com.example.qa.sprint2.department;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.rbac.entity.RoleDepartmentMap;
import com.example.rbac.repository.RoleDepartmentMapRepository;

@DataJpaTest
class DepartmentRepositoryTest {

    @Autowired
    private RoleDepartmentMapRepository repository;

    private UUID roleOne;
    private UUID roleTwo;

    private UUID department101;
    private UUID department102;
    private UUID department103;
    private UUID department201;

    @BeforeEach
    void setUp() {

        repository.deleteAll();

        roleOne = UUID.randomUUID();
        roleTwo = UUID.randomUUID();

        department101 = UUID.randomUUID();
        department102 = UUID.randomUUID();
        department103 = UUID.randomUUID();
        department201 = UUID.randomUUID();

        repository.save(
                createMap(roleOne, department101)
        );

        repository.save(
                createMap(roleOne, department102)
        );

        repository.save(
                createMap(roleOne, department103)
        );

        repository.save(
                createMap(roleTwo, department201)
        );
    }

    // =========================================================
    // FIND
    // =========================================================

    @Test
    void findByUserRoleId_shouldReturnMappingsForRole() {

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(roleOne);

        assertEquals(3, result.size());

        assertTrue(
                result.stream()
                        .allMatch(map ->
                                roleOne.equals(
                                        map.getUserRoleId()
                                ))
        );
    }

    @Test
    void findByUserRoleId_shouldNotReturnOtherRoleMappings() {

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(roleOne);

        assertTrue(
                result.stream()
                        .noneMatch(map ->
                                roleTwo.equals(
                                        map.getUserRoleId()
                                ))
        );
    }

    @Test
    void findByUserRoleId_shouldReturnEmptyWhenNoMappingsExist() {

        UUID unknownRole = UUID.randomUUID();

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(unknownRole);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserRoleId_shouldReturnCorrectDepartmentIds() {

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(roleOne);

        List<UUID> departmentIds =
                result.stream()
                        .map(RoleDepartmentMap::getDepartmentId)
                        .toList();

        assertTrue(
                departmentIds.contains(department101)
        );

        assertTrue(
                departmentIds.contains(department102)
        );

        assertTrue(
                departmentIds.contains(department103)
        );

        assertTrue(
                departmentIds.stream()
                        .noneMatch(
                                department201::equals
                        )
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void deleteByUserRoleId_shouldDeleteOnlySelectedRoleMappings() {

        repository.deleteByUserRoleId(roleOne);

        List<RoleDepartmentMap> roleOneMappings =
                repository.findByUserRoleId(roleOne);

        List<RoleDepartmentMap> roleTwoMappings =
                repository.findByUserRoleId(roleTwo);

        assertTrue(roleOneMappings.isEmpty());

        assertEquals(
                1,
                roleTwoMappings.size()
        );

        assertEquals(
                department201,
                roleTwoMappings.get(0).getDepartmentId()
        );
    }

    @Test
    void deleteByUserRoleId_shouldDoNothingForUnknownRole() {

        UUID unknownRole = UUID.randomUUID();

        repository.deleteByUserRoleId(unknownRole);

        assertEquals(
                3,
                repository.findByUserRoleId(roleOne).size()
        );

        assertEquals(
                1,
                repository.findByUserRoleId(roleTwo).size()
        );
    }

    // =========================================================
    // SAVE
    // =========================================================

    @Test
    void save_shouldPersistMapping() {

        UUID roleThree = UUID.randomUUID();
        UUID department301 = UUID.randomUUID();

        RoleDepartmentMap map =
                createMap(
                        roleThree,
                        department301
                );

        RoleDepartmentMap saved =
                repository.save(map);

        assertNotNull(saved.getId());

        List<RoleDepartmentMap> result =
                repository.findByUserRoleId(roleThree);

        assertEquals(1, result.size());

        assertEquals(
                department301,
                result.get(0).getDepartmentId()
        );

        assertEquals(
                roleThree,
                result.get(0).getUserRoleId()
        );
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
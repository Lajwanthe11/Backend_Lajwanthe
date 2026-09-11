package com.example.qa.sprint2.department;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.rbac.RbacApplication;
import org.springframework.test.context.ContextConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.rbac.controller.DepartmentPermissionController;
import com.example.rbac.service.DepartmentPermissionService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentPermissionController.class)
class DepartmentAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentPermissionService departmentPermissionService;

    // =========================================================
    // GET
    // =========================================================

    @Test
    void shouldGetDepartmentScope() throws Exception {

        when(departmentPermissionService
                .getDepartmentScope(10L))
                .thenReturn(
                        List.of(101L, 102L, 103L)
                );

        mockMvc.perform(
                get(
                        "/api/v1/users/1/roles/10/departments"
                )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0]").value(101))
        .andExpect(jsonPath("$[1]").value(102))
        .andExpect(jsonPath("$[2]").value(103));

        verify(departmentPermissionService)
                .getDepartmentScope(10L);
    }

    @Test
    void shouldReturnEmptyDepartmentScope() throws Exception {

        when(departmentPermissionService
                .getDepartmentScope(10L))
                .thenReturn(List.of());

        mockMvc.perform(
                get(
                        "/api/v1/users/1/roles/10/departments"
                )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

        verify(departmentPermissionService)
                .getDepartmentScope(10L);
    }

    @Test
    void shouldUseRoleIdFromPathForGet() throws Exception {

        when(departmentPermissionService
                .getDepartmentScope(99L))
                .thenReturn(List.of(500L));

        mockMvc.perform(
                get(
                        "/api/v1/users/123/roles/99/departments"
                )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value(500));

        verify(departmentPermissionService)
                .getDepartmentScope(99L);

        verify(departmentPermissionService, never())
                .getDepartmentScope(123L);
    }

    // =========================================================
    // PUT
    // =========================================================

    @Test
    void shouldUpdateDepartmentScope() throws Exception {

        doNothing()
                .when(departmentPermissionService)
                .updateDepartmentScope(
                        eq(10L),
                        eq(List.of(101L, 102L))
                );

        mockMvc.perform(
                put(
                        "/api/v1/users/1/roles/10/departments"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": [101, 102]
                    }
                    """)
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .updateDepartmentScope(
                        10L,
                        List.of(101L, 102L)
                );
    }

    @Test
    void shouldUpdateDepartmentScopeWithSingleDepartment()
            throws Exception {

        mockMvc.perform(
                put(
                        "/api/v1/users/1/roles/10/departments"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": [101]
                    }
                    """)
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .updateDepartmentScope(
                        10L,
                        List.of(101L)
                );
    }

    @Test
    void shouldPassEmptyDepartmentListToService()
            throws Exception {

        mockMvc.perform(
                put(
                        "/api/v1/users/1/roles/10/departments"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": []
                    }
                    """)
        )
        .andExpect(status().isOk());

        /*
         * Controller itself has no validation.
         * Service is responsible for rejecting empty lists.
         */
        verify(departmentPermissionService)
                .updateDepartmentScope(
                        10L,
                        List.of()
                );
    }

    @Test
    void shouldUseRoleIdFromPathForUpdate()
            throws Exception {

        mockMvc.perform(
                put(
                        "/api/v1/users/5/roles/77/departments"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": [201, 202]
                    }
                    """)
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .updateDepartmentScope(
                        77L,
                        List.of(201L, 202L)
                );

        verify(departmentPermissionService, never())
                .updateDepartmentScope(
                        eq(5L),
                        anyList()
                );
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void shouldRemoveAllDepartmentScope()
            throws Exception {

        mockMvc.perform(
                delete(
                        "/api/v1/users/1/roles/10/departments/all"
                )
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .removeAllDepartmentScope(10L);
    }

    @Test
    void shouldUseRoleIdFromPathForDelete()
            throws Exception {

        mockMvc.perform(
                delete(
                        "/api/v1/users/55/roles/99/departments/all"
                )
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .removeAllDepartmentScope(99L);

        verify(departmentPermissionService, never())
                .removeAllDepartmentScope(55L);
    }

    // =========================================================
    // INVALID JSON / MISSING BODY FIELD
    // =========================================================

    @Test
    void shouldPassMissingDepartmentIdsAsNullToService()
            throws Exception {

        mockMvc.perform(
                put(
                        "/api/v1/users/1/roles/10/departments"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {}
                    """)
        )
        .andExpect(status().isOk());

        verify(departmentPermissionService)
                .updateDepartmentScope(
                        10L,
                        null
                );
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {

        mockMvc.perform(
                put(
                        "/api/v1/users/1/roles/10/departments"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "departmentIds": [
                    """)
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(
                departmentPermissionService
        );
    }
}
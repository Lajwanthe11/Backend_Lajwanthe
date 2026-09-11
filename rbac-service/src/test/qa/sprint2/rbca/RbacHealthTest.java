package com.example.qa.sprint2.rbac;

import com.example.rbac.controller.RbacPermissionController;
import com.example.rbac.service.PermissionCacheService;
import com.example.rbac.service.PermissionCheckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.rbac.RbacApplication;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RbacPermissionController.class)
class RbacHealthTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionCheckService permissionCheckService;

    @MockBean
    private PermissionCacheService permissionCacheService;

    @Test
    void shouldReturnRbacHealthUp() throws Exception {

        mockMvc.perform(
                get("/api/v1/rbac/health")
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(
                "application/json"
        ))
        .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldExposeCorrectHealthEndpoint() throws Exception {

        mockMvc.perform(
                get("/api/v1/rbac/health")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").exists());
    }
}

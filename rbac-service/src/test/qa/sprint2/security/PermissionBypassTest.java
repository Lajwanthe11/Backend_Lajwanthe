package com.example.rbac.security;

import com.example.rbac.service.PermissionAuthorizationService;
import com.example.rbac.service.PermissionDeniedException;
import com.example.rbac.service.PermissionResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PermissionBypassTest {

    @Autowired
    private PermissionAuthorizationService authorizationService;

    @MockBean
    private PermissionResolver permissionResolver;

    @Test
    void userWithoutRequiredPermission_shouldBeDenied() {

        when(permissionResolver.hasPermission(
                "user-readonly-1",
                "USER_CREATE"
        )).thenReturn(false);

        var annotation = getRequirePermission("USER_CREATE");

        PermissionDeniedException exception =
                assertThrows(
                        PermissionDeniedException.class,
                        () -> authorizationService.authorize(annotation)
                );

        assertEquals(
                "USER_CREATE",
                exception.getRequestedPermission()
        );
    }

    @Test
    void userWithRequiredPermission_shouldBeAllowed() {

        when(permissionResolver.hasPermission(
                "user-admin-1",
                "USER_CREATE"
        )).thenReturn(true);

        var annotation = getRequirePermission("USER_CREATE");

        assertDoesNotThrow(
                () -> authorizationService.authorize(annotation)
        );
    }

    @Test
    void requireAll_whenOnePermissionMissing_shouldDeny() {

        when(permissionResolver.hasPermission(
                "user-hr-1",
                "USER_READ"
        )).thenReturn(true);

        when(permissionResolver.hasPermission(
                "user-hr-1",
                "USER_UPDATE"
        )).thenReturn(false);

        var annotation = getRequireAllPermission(
                "USER_READ",
                "USER_UPDATE"
        );

        PermissionDeniedException exception =
                assertThrows(
                        PermissionDeniedException.class,
                        () -> authorizationService.authorize(annotation)
                );

        assertEquals(
                "USER_UPDATE",
                exception.getRequestedPermission()
        );
    }

    @Test
    void requireAny_whenOnePermissionExists_shouldAllow() {

        when(permissionResolver.hasPermission(
                "user-readonly-1",
                "REPORT_VIEW"
        )).thenReturn(true);

        when(permissionResolver.hasPermission(
                "user-readonly-1",
                "REPORT_EXPORT"
        )).thenReturn(false);

        var annotation = getRequireAnyPermission(
                "REPORT_VIEW",
                "REPORT_EXPORT"
        );

        assertDoesNotThrow(
                () -> authorizationService.authorize(annotation)
        );
    }

    @Test
    void emptyPermissionAnnotation_shouldFailClosed() {

        var annotation = getEmptyPermission();

        assertThrows(
                PermissionDeniedException.class,
                () -> authorizationService.authorize(annotation)
        );
    }

    private com.example.rbac.config.RequirePermission
    getRequirePermission(String permission) {

        return PermissionBypassTest.class
                .getDeclaredMethod("dummyMethod", String.class)
                .getAnnotation(
                        com.example.rbac.config.RequirePermission.class
                );
    }

    private com.example.rbac.config.RequirePermission
    getRequireAllPermission(String first, String second) {

        return PermissionBypassTest.class
                .getDeclaredMethod("requireAllDummy")
                .getAnnotation(
                        com.example.rbac.config.RequirePermission.class
                );
    }

    private com.example.rbac.config.RequirePermission
    getRequireAnyPermission(String first, String second) {

        return PermissionBypassTest.class
                .getDeclaredMethod("requireAnyDummy")
                .getAnnotation(
                        com.example.rbac.config.RequirePermission.class
                );
    }

    private com.example.rbac.config.RequirePermission
    getEmptyPermission() {

        return PermissionBypassTest.class
                .getDeclaredMethod("emptyDummy")
                .getAnnotation(
                        com.example.rbac.config.RequirePermission.class
                );
    }

    @com.example.rbac.config.RequirePermission("USER_CREATE")
    private void dummyMethod(String value) {
    }

    @com.example.rbac.config.RequirePermission(
            requireAll = {"USER_READ", "USER_UPDATE"}
    )
    private void requireAllDummy() {
    }

    @com.example.rbac.config.RequirePermission(
            requireAny = {"REPORT_VIEW", "REPORT_EXPORT"}
    )
    private void requireAnyDummy() {
    }

    @com.example.rbac.config.RequirePermission
    private void emptyDummy() {
    }
}
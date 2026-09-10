package com.example.rbac.service;

import com.example.rbac.config.RequirePermission;
import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises the requireAll / requireAny evaluation logic directly:
 * - user has required permission -> executes
 * - user lacks required permission -> 403 (PermissionDeniedException)
 * - requireAll with one missing -> 403
 * - requireAny with one matching -> executes
 * - requireAny with none matching -> 403
 */
class PermissionAuthorizationServiceTest {

    private PermissionResolver permissionResolver;
    private SecurityContextUtil securityContextUtil;
    private PermissionAuthorizationService service;

    private static final AuthenticatedUser USER = new AuthenticatedUser("user-1", "tenant-1");

    @BeforeEach
    void setUp() {
        permissionResolver = mock(PermissionResolver.class);
        securityContextUtil = mock(SecurityContextUtil.class);
        when(securityContextUtil.currentUser()).thenReturn(USER);
        service = new PermissionAuthorizationService(permissionResolver, securityContextUtil);
    }

    @Test
    void allowsWhenUserHasSingleRequiredPermission() {
        when(permissionResolver.hasPermission("user-1", "USER_CREATE")).thenReturn(true);
        service.authorize(annotation(new String[] { "USER_CREATE" }, new String[] {}, new String[] {}));
    }

    @Test
    void deniesWhenUserLacksSingleRequiredPermission() {
        when(permissionResolver.hasPermission("user-1", "USER_CREATE")).thenReturn(false);
        assertThatThrownBy(
                () -> service.authorize(annotation(new String[] { "USER_CREATE" }, new String[] {}, new String[] {})))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void requireAllDeniesWhenOnePermissionIsMissing() {
        when(permissionResolver.hasPermission("user-1", "USER_READ")).thenReturn(true);
        when(permissionResolver.hasPermission("user-1", "USER_UPDATE")).thenReturn(false);
        assertThatThrownBy(() -> service.authorize(
                annotation(new String[] {}, new String[] { "USER_READ", "USER_UPDATE" }, new String[] {})))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void requireAllAllowsWhenBothPermissionsPresent() {
        when(permissionResolver.hasPermission("user-1", "USER_READ")).thenReturn(true);
        when(permissionResolver.hasPermission("user-1", "USER_UPDATE")).thenReturn(true);
        service.authorize(annotation(new String[] {}, new String[] { "USER_READ", "USER_UPDATE" }, new String[] {}));
    }

    @Test
    void requireAnyAllowsWhenOneOfSeveralPermissionsPresent() {
        when(permissionResolver.hasPermission("user-1", "REPORT_VIEW")).thenReturn(false);
        when(permissionResolver.hasPermission("user-1", "REPORT_EXPORT")).thenReturn(true);
        service.authorize(
                annotation(new String[] {}, new String[] {}, new String[] { "REPORT_VIEW", "REPORT_EXPORT" }));
    }

    @Test
    void requireAnyDeniesWhenNoneMatch() {
        when(permissionResolver.hasPermission("user-1", "REPORT_VIEW")).thenReturn(false);
        when(permissionResolver.hasPermission("user-1", "REPORT_EXPORT")).thenReturn(false);
        assertThatThrownBy(() -> service.authorize(
                annotation(new String[] {}, new String[] {}, new String[] { "REPORT_VIEW", "REPORT_EXPORT" })))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void deniedExceptionNeverExposesPermissionInGenericMessage() {
        when(permissionResolver.hasPermission("user-1", "USER_DELETE")).thenReturn(false);
        assertThatThrownBy(
                () -> service.authorize(annotation(new String[] { "USER_DELETE" }, new String[] {}, new String[] {})))
                .isInstanceOfSatisfying(PermissionDeniedException.class,
                        ex -> assertThat(ex.getRequestedPermission()).isEqualTo("USER_DELETE"));
        // the internal detail above is for logging only - GlobalExceptionHandler
        // never serializes getRequestedPermission() into the HTTP response.
    }

    private RequirePermission annotation(String[] value, String[] requireAll, String[] requireAny) {
        return new RequirePermission() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return RequirePermission.class;
            }

            @Override
            public String[] value() {
                return value;
            }

            @Override
            public String[] requireAll() {
                return requireAll;
            }

            @Override
            public String[] requireAny() {
                return requireAny;
            }

            @Override
            public String toString() {
                return "@RequirePermission(value=" + Arrays.toString(value)
                        + ", requireAll=" + Arrays.toString(requireAll)
                        + ", requireAny=" + Arrays.toString(requireAny) + ")";
            }
        };
    }
}

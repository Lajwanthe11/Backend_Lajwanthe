package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.config.RequirePermission;
import com.example.platformadmin.rbac.util.SecurityContextUtil;
import com.example.platformadmin.rbac.dto.response.AuthenticatedUser;
import com.example.platformadmin.rbac.exception.PermissionDeniedException;
import com.example.platformadmin.rbac.service.serviceImpl.PermissionAuthorizationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionAuthorizationServiceTest {

        @Mock
        private PermissionResolver permissionResolver;

        @Mock
        private SecurityContextUtil securityContextUtil;

        private PermissionAuthorizationService authorizationService;

        private static final String USER_ID = "user-1";
        private static final String TENANT_ID = "tenant-acme";

        @BeforeEach
        void setUp() {
                authorizationService = new PermissionAuthorizationService(
                                permissionResolver, securityContextUtil);

                when(securityContextUtil.currentUser())
                                .thenReturn(new AuthenticatedUser(USER_ID, TENANT_ID));
        }

        // ------------------------------------------------------------------
        // requireAll / value[] — must hold every listed permission
        // ------------------------------------------------------------------

        @Test
        void authorize_requireAll_allPresent_proceeds() {
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of("USER_READ", "USER_CREATE"));

                assertDoesNotThrow(() -> authorizationService.authorize(
                                annotation(new String[] { "USER_READ", "USER_CREATE" }, new String[] {},
                                                new String[] {})));
        }

        @Test
        void authorize_requireAll_oneMissing_throwsPermissionDenied() {
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of("USER_READ")); // USER_CREATE absent

                assertThrows(PermissionDeniedException.class, () -> authorizationService.authorize(
                                annotation(new String[] { "USER_READ", "USER_CREATE" }, new String[] {},
                                                new String[] {})));
        }

        // ------------------------------------------------------------------
        // requireAny — must hold at least one listed permission
        // ------------------------------------------------------------------

        @Test
        void authorize_requireAny_onePresent_proceeds() {
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of("REPORT_VIEW"));

                assertDoesNotThrow(() -> authorizationService.authorize(
                                annotation(new String[] {}, new String[] {},
                                                new String[] { "USER_CREATE", "REPORT_VIEW" })));
        }

        @Test
        void authorize_requireAny_nonePresent_throwsPermissionDenied() {
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of("USER_READ")); // neither USER_CREATE nor REPORT_EXPORT

                assertThrows(PermissionDeniedException.class, () -> authorizationService.authorize(
                                annotation(new String[] {}, new String[] {},
                                                new String[] { "USER_CREATE", "REPORT_EXPORT" })));
        }

        // ------------------------------------------------------------------
        // Empty annotation — fail closed
        // ------------------------------------------------------------------

        @Test
        void authorize_emptyAnnotation_throwsPermissionDenied() {
                // resolvePermissions may or may not be called depending on ordering,
                // but the service must reject an annotation with no codes configured.
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of());

                assertThrows(PermissionDeniedException.class, () -> authorizationService.authorize(
                                annotation(new String[] {}, new String[] {}, new String[] {})));
        }

        // ------------------------------------------------------------------
        // SUPER_ADMIN wildcard — bypasses all checks
        // ------------------------------------------------------------------

        @Test
        void authorize_superAdmin_wildcardBypassesRequireAll() {
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of("*"));

                assertDoesNotThrow(() -> authorizationService.authorize(
                                annotation(new String[] { "USER_DELETE", "BILLING_MANAGE" }, new String[] {},
                                                new String[] {})));
        }

        @Test
        void authorize_superAdmin_wildcardBypassesRequireAny() {
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of("*"));

                assertDoesNotThrow(() -> authorizationService.authorize(
                                annotation(new String[] {}, new String[] {},
                                                new String[] { "USER_DELETE", "BILLING_MANAGE" })));
        }

        // ------------------------------------------------------------------
        // Tenant forwarding — the real tenantId must reach resolvePermissions
        // ------------------------------------------------------------------

        @Test
        void authorize_tenantIdFromJwtIsForwardedToResolver() {
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of("USER_READ"));

                authorizationService.authorize(
                                annotation(new String[] { "USER_READ" }, new String[] {}, new String[] {}));

                // Verify the resolver was called with the tenant from the JWT,
                // not with "" (the broken default) or any other value.
                verify(permissionResolver).resolvePermissions(USER_ID, TENANT_ID);
        }

        @Test
        void authorize_nullTenantId_fallsBackToEmptyString() {
                when(securityContextUtil.currentUser())
                                .thenReturn(new AuthenticatedUser(USER_ID, null));
                when(permissionResolver.resolvePermissions(USER_ID, ""))
                                .thenReturn(Set.of("USER_READ"));

                assertDoesNotThrow(() -> authorizationService.authorize(
                                annotation(new String[] { "USER_READ" }, new String[] {}, new String[] {})));

                verify(permissionResolver).resolvePermissions(USER_ID, "");
        }

        // ------------------------------------------------------------------
        // requireAll via requireAll[] field (not value[])
        // ------------------------------------------------------------------

        @Test
        void authorize_requireAllField_allPresent_proceeds() {
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of("ROLE_ASSIGN", "ROLE_READ"));

                assertDoesNotThrow(() -> authorizationService.authorize(
                                annotation(new String[] {}, new String[] { "ROLE_ASSIGN", "ROLE_READ" },
                                                new String[] {})));
        }

        @Test
        void authorize_valueAndRequireAllAreMerged() {
                // value[] = {"USER_READ"}, requireAll[] = {"USER_CREATE"} → both required
                when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                                .thenReturn(Set.of("USER_READ")); // USER_CREATE absent

                assertThrows(PermissionDeniedException.class, () -> authorizationService.authorize(
                                annotation(new String[] { "USER_READ" }, new String[] { "USER_CREATE" },
                                                new String[] {})));
        }

        // ------------------------------------------------------------------
        // Helper: build a @RequirePermission annotation instance at runtime
        // ------------------------------------------------------------------

        private static RequirePermission annotation(
                        String[] value,
                        String[] requireAll,
                        String[] requireAny) {

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
                };
        }
}

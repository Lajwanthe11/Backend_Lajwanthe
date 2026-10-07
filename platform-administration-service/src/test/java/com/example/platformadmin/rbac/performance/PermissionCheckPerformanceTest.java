package com.example.platformadmin.rbac.performance;

import com.example.platformadmin.rbac.service.PermissionCheckService;
import com.example.platformadmin.rbac.service.PermissionResolver;
import com.example.platformadmin.rbac.service.serviceImpl.PermissionCheckServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionCheckPerformanceTest {

    @Mock
    private PermissionResolver permissionResolver;

    private PermissionCheckService permissionCheckService;

    private static final String USER_ID = "user-1001";
    private static final String TENANT_ID = "tenant-1001";
    private static final String PERMISSION_CODE = "USER_READ";

    private static final int WARMUP_ITERATIONS = 100;
    private static final int MEASUREMENT_ITERATIONS = 1000;

    @BeforeEach
    void setUp() {
        permissionCheckService =
                new PermissionCheckServiceImpl(permissionResolver);
    }

    @Test
    @DisplayName("Permission check from cache should remain within 10 milliseconds")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void permissionCheckFromCache_shouldBeWithinTenMilliseconds() {

        when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                .thenReturn(Set.of("USER_READ", "USER_UPDATE"));

        warmUp();

        long[] executionTimes = new long[MEASUREMENT_ITERATIONS];

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {

            long startTime = System.nanoTime();

            boolean result = permissionCheckService.hasPermission(
                    USER_ID,
                    TENANT_ID,
                    PERMISSION_CODE);

            long endTime = System.nanoTime();

            executionTimes[i] = endTime - startTime;

            assertTrue(result);
        }

        long p95Nanoseconds =
                PerformanceTestUtils.percentile(executionTimes, 0.95);

        long p95Milliseconds =
                TimeUnit.NANOSECONDS.toMillis(p95Nanoseconds);

        double averageNanoseconds =
                PerformanceTestUtils.average(executionTimes);

        double averageMilliseconds =
                averageNanoseconds / 1_000_000.0;

        System.out.println("Cache permission-check average: "
                + averageMilliseconds + " ms");

        System.out.println("Cache permission-check P95: "
                + p95Milliseconds + " ms");

        assertTrue(
                p95Milliseconds < 10,
                "Cache permission check P95 should be below 10 ms, but was "
                        + p95Milliseconds + " ms");
    }

    @Test
    @DisplayName("Denied permission check should remain within 10 milliseconds")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deniedPermissionCheck_shouldBeWithinTenMilliseconds() {

        when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                .thenReturn(Set.of("USER_READ"));

        warmUp();

        long[] executionTimes = new long[MEASUREMENT_ITERATIONS];

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {

            long startTime = System.nanoTime();

            boolean result = permissionCheckService.hasPermission(
                    USER_ID,
                    TENANT_ID,
                    "USER_DELETE");

            long endTime = System.nanoTime();

            executionTimes[i] = endTime - startTime;

            assertTrue(!result);
        }

        long p95Nanoseconds =
                PerformanceTestUtils.percentile(executionTimes, 0.95);

        long p95Milliseconds =
                TimeUnit.NANOSECONDS.toMillis(p95Nanoseconds);

        System.out.println("Denied permission-check P95: "
                + p95Milliseconds + " ms");

        assertTrue(
                p95Milliseconds < 10,
                "Denied permission check P95 should be below 10 ms, but was "
                        + p95Milliseconds + " ms");
    }

    @Test
    @DisplayName("Wildcard permission check should remain within 10 milliseconds")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void wildcardPermissionCheck_shouldBeWithinTenMilliseconds() {

        when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                .thenReturn(Set.of("*"));

        warmUp();

        long[] executionTimes = new long[MEASUREMENT_ITERATIONS];

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {

            long startTime = System.nanoTime();

            boolean result = permissionCheckService.hasPermission(
                    USER_ID,
                    TENANT_ID,
                    "ANY_PERMISSION");

            long endTime = System.nanoTime();

            executionTimes[i] = endTime - startTime;

            assertTrue(result);
        }

        long p95Nanoseconds =
                PerformanceTestUtils.percentile(executionTimes, 0.95);

        long p95Milliseconds =
                TimeUnit.NANOSECONDS.toMillis(p95Nanoseconds);

        System.out.println("Wildcard permission-check P95: "
                + p95Milliseconds + " ms");

        assertTrue(
                p95Milliseconds < 10,
                "Wildcard permission check P95 should be below 10 ms, but was "
                        + p95Milliseconds + " ms");
    }

    @Test
    @DisplayName("Permission checks must preserve tenant and user isolation")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void permissionCheck_shouldPreserveTenantAndUserIsolation() {

        String userOne = "user-1001";
        String userTwo = "user-2002";

        String tenantOne = "tenant-1001";
        String tenantTwo = "tenant-2002";

        when(permissionResolver.resolvePermissions(userOne, tenantOne))
                .thenReturn(Set.of("USER_READ"));

        when(permissionResolver.resolvePermissions(userTwo, tenantTwo))
                .thenReturn(Set.of("USER_DELETE"));

        warmUp();

        long startTime = System.nanoTime();

        boolean userOneAllowed = permissionCheckService.hasPermission(
                userOne,
                tenantOne,
                "USER_READ");

        boolean userOneWrongPermission = permissionCheckService.hasPermission(
                userOne,
                tenantOne,
                "USER_DELETE");

        boolean userTwoAllowed = permissionCheckService.hasPermission(
                userTwo,
                tenantTwo,
                "USER_DELETE");

        boolean userTwoWrongPermission = permissionCheckService.hasPermission(
                userTwo,
                tenantTwo,
                "USER_READ");

        long endTime = System.nanoTime();

        long executionTimeMilliseconds =
                TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        assertTrue(userOneAllowed);
        assertTrue(!userOneWrongPermission);
        assertTrue(userTwoAllowed);
        assertTrue(!userTwoWrongPermission);

        assertTrue(
                executionTimeMilliseconds < 10,
                "Tenant and user isolation checks should complete quickly");
    }

    @Test
    @DisplayName("Resolved permissions should be returned without unnecessary processing")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resolvedPermissions_shouldBeReturnedWithinPerformanceLimit() {

        Set<String> permissions = Set.of(
                "USER_READ",
                "USER_CREATE",
                "USER_UPDATE",
                "USER_DELETE",
                "ROLE_READ",
                "ROLE_CREATE",
                "ROLE_UPDATE",
                "ROLE_DELETE",
                "PERMISSION_READ",
                "PERMISSION_UPDATE");

        when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                .thenReturn(permissions);

        warmUp();

        long[] executionTimes = new long[MEASUREMENT_ITERATIONS];

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {

            long startTime = System.nanoTime();

            Set<String> result =
                    permissionCheckService.getResolvedPermissions(
                            USER_ID,
                            TENANT_ID);

            long endTime = System.nanoTime();

            executionTimes[i] = endTime - startTime;

            assertEquals(permissions, result);
        }

        long p95Nanoseconds =
                PerformanceTestUtils.percentile(executionTimes, 0.95);

        long p95Milliseconds =
                TimeUnit.NANOSECONDS.toMillis(p95Nanoseconds);

        System.out.println("Resolved permissions P95: "
                + p95Milliseconds + " ms");

        assertTrue(
                p95Milliseconds < 10,
                "Resolved permission retrieval P95 should be below 10 ms, but was "
                        + p95Milliseconds + " ms");
    }

    @Test
    @DisplayName("Database fallback permission check should be within 100 milliseconds")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void databaseFallbackPermissionCheck_shouldBeWithinOneHundredMilliseconds()
            throws InterruptedException {

        /*
         * This test simulates the database fallback path.
         *
         * The actual PermissionResolver implementation is responsible
         * for Redis cache lookup and database fallback.
         *
         * The mock delay represents a database response that completes
         * within the Sprint 2 requirement.
         */

        when(permissionResolver.resolvePermissions(USER_ID, TENANT_ID))
                .thenAnswer(invocation -> {

                    Thread.sleep(5);

                    return Set.of("USER_READ", "USER_UPDATE");
                });

        long startTime = System.nanoTime();

        boolean result = permissionCheckService.hasPermission(
                USER_ID,
                TENANT_ID,
                PERMISSION_CODE);

        long endTime = System.nanoTime();

        long executionTimeMilliseconds =
                TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        assertTrue(result);

        System.out.println("Database fallback permission-check time: "
                + executionTimeMilliseconds + " ms");

        assertTrue(
                executionTimeMilliseconds < 100,
                "Database fallback permission check should be below 100 ms, but was "
                        + executionTimeMilliseconds + " ms");
    }

    private void warmUp() {

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {

            permissionCheckService.hasPermission(
                    USER_ID,
                    TENANT_ID,
                    PERMISSION_CODE);
        }
    }
}

package com.example.platformadmin.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.platformadmin.rbac.service.serviceImpl.PermissionCacheServiceImpl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class PermissionCacheServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    private SimpleMeterRegistry meterRegistry;

    private PermissionCacheServiceImpl permissionCacheService;

    @BeforeEach
    void setUp() {

        meterRegistry = new SimpleMeterRegistry();

        permissionCacheService =
                new PermissionCacheServiceImpl(
                        redisTemplate,
                        meterRegistry);
    }

    @AfterEach
    void tearDown() {
        meterRegistry.close();
    }

    @Test
    void clearUserPermissionsCache_deletesUserCache() {

        permissionCacheService.clearUserPermissionsCache(
                "user1",
                "tenant1");

        verify(redisTemplate).delete(
                "perms:tenant1:user1");
    }

    @Test
    void clearUsersPermissionsCache_deletesCacheForAllUsers() {

        Set<String> userIds = Set.of(
                "user1",
                "user2",
                "user3");

        permissionCacheService.clearUsersPermissionsCache(
                userIds,
                "tenant1");

        verify(redisTemplate).delete(
                "perms:tenant1:user1");

        verify(redisTemplate).delete(
                "perms:tenant1:user2");

        verify(redisTemplate).delete(
                "perms:tenant1:user3");
    }

    @Test
    void clearUserPermissionsCache_redisFailure_doesNotThrowException() {

        doThrow(new RuntimeException("Redis unavailable"))
                .when(redisTemplate)
                .delete("perms:tenant1:user1");

        permissionCacheService.clearUserPermissionsCache(
                "user1",
                "tenant1");

        verify(redisTemplate).delete(
                "perms:tenant1:user1");

        assertEquals(
                1.0,
                meterRegistry
                        .counter("redis.cache.failures")
                        .count());
    }
}
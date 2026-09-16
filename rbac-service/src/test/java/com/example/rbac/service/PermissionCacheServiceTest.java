package com.example.rbac.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class PermissionCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    private PermissionCacheService permissionCacheService;

    @BeforeEach
    void setUp() {
        permissionCacheService = new PermissionCacheService(redisTemplate);
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
    }
}

package com.example.rbac.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    public PermissionCacheService(
            RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void clearUserPermissionsCache(
            String userId,
            String tenantId) {

        String cacheKey =
                "perms:" + tenantId + ":" + userId;

        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            // Redis unavailable; permission resolution will fall back to DB
        }
    }

    public void clearUsersPermissionsCache(
            Set<String> userIds,
            String tenantId) {

        for (String userId : userIds) {
            clearUserPermissionsCache(userId, tenantId);
        }
    }
}
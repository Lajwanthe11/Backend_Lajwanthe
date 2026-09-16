package com.example.rbac.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionCacheService {

    private static final Logger log =
            LoggerFactory.getLogger(PermissionCacheService.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final Counter redisCacheFailures;

    public PermissionCacheService(
            RedisTemplate<String, String> redisTemplate,
            MeterRegistry meterRegistry) {

        this.redisTemplate = redisTemplate;

        this.redisCacheFailures =
                Counter.builder("redis.cache.failures")
                        .description(
                                "Number of Redis permission cache failures")
                        .register(meterRegistry);
    }

    public void clearUserPermissionsCache(
            String userId,
            String tenantId) {

        String cacheKey =
                "perms:" + tenantId + ":" + userId;

        try {
            redisTemplate.delete(cacheKey);

        } catch (Exception e) {

            log.warn(
                    "Failed to clear permission cache for userId={}, tenantId={}",
                    userId,
                    tenantId,
                    e);

            redisCacheFailures.increment();
        }
    }

    public void clearUsersPermissionsCache(
            Set<String> userIds,
            String tenantId) {

        for (String userId : userIds) {
            clearUserPermissionsCache(
                    userId,
                    tenantId);
        }
    }
}
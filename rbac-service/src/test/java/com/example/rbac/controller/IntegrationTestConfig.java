package com.example.rbac.controller;

import com.example.rbac.service.PermissionResolver;
import com.example.rbac.service.StubPermissionResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.mock;

/**
 * Test-only Spring configuration that:
 * <ol>
 *   <li>Registers {@link StubPermissionResolver} as the <em>primary</em>
 *       {@link PermissionResolver} bean so the stub wins over
 *       {@code PermissionResolverImpl} (which needs a live Redis + PostgreSQL).</li>
 *   <li>Provides a no-op {@code RedisTemplate} mock so that beans like
 *       {@code PermissionCacheService} and {@code PermissionResolverImpl} that
 *       require it can still be instantiated – they are never actually called
 *       during these tests because the stub resolver intercepts all lookups.</li>
 * </ol>
 */
@TestConfiguration
public class IntegrationTestConfig {

    /**
     * Mock the low-level connection factory so nothing tries to open a TCP
     * socket to a Redis server during tests.
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }

    /**
     * Expose a mock RedisTemplate so that Spring can wire all beans that
     * declare a RedisTemplate dependency, even though no Redis server is
     * running during tests.
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, String> redisTemplate() {
        return mock(RedisTemplate.class);
    }

    /**
     * Make StubPermissionResolver the primary PermissionResolver for all
     * integration tests, overriding the production PermissionResolverImpl.
     */
    @Bean
    @Primary
    public PermissionResolver permissionResolver() {
        return new StubPermissionResolver();
    }
}

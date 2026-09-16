package com.example.microservice.auth.security.jwt;

import com.example.microservice.common.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 86400000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationInMs", 604800000L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testGenerateAndValidateTokenWithDefaultTenant() {
        String token = jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromJWT(token));
        assertEquals("ROLE_USER", jwtTokenProvider.getRolesFromJWT(token));
        assertEquals(TenantContext.DEFAULT_TENANT_ID, jwtTokenProvider.getTenantIdFromJWT(token));
    }

    @Test
    void testGenerateAndValidateTokenWithCustomTenant() {
        String token = jwtTokenProvider.generateAccessToken("testuser", "ROLE_ADMIN", "tenant-xyz");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromJWT(token));
        assertEquals("ROLE_ADMIN", jwtTokenProvider.getRolesFromJWT(token));
        assertEquals("tenant-xyz", jwtTokenProvider.getTenantIdFromJWT(token));
    }

    @Test
    void testGenerateRefreshTokenWithTenant() {
        String refreshToken = jwtTokenProvider.generateRefreshToken("testuser", "tenant-abc");
        assertNotNull(refreshToken);
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromJWT(refreshToken));
        assertEquals("tenant-abc", jwtTokenProvider.getTenantIdFromJWT(refreshToken));
    }
}

package com.example.microservice;

import com.example.microservice.auth.security.jwt.JwtTokenProvider;
import com.example.microservice.auth.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.example.microservice.auth.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.example.microservice.common.tenant.TenantContext;
import com.example.microservice.common.tenant.TenantFilter;
import com.example.microservice.config.TenantIdentifierResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BaseMicroserviceApplicationTests {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler failureHandler;

    @Autowired
    private TenantFilter tenantFilter;

    @Autowired
    private TenantIdentifierResolver tenantIdentifierResolver;

    @Test
    void contextLoads() {
        assertNotNull(jwtTokenProvider);
        assertNotNull(successHandler);
        assertNotNull(failureHandler);
        assertNotNull(tenantFilter);
        assertNotNull(tenantIdentifierResolver);

        // Verify values are injected from yml configuration
        String secret = (String) ReflectionTestUtils.getField(jwtTokenProvider, "jwtSecret");
        Long expiration = (Long) ReflectionTestUtils.getField(jwtTokenProvider, "jwtExpirationInMs");
        Long refreshExpiration = (Long) ReflectionTestUtils.getField(jwtTokenProvider, "refreshExpirationInMs");

        @SuppressWarnings("unchecked")
        List<String> successRedirectUris = (List<String>) ReflectionTestUtils.getField(successHandler, "authorizedRedirectUris");

        @SuppressWarnings("unchecked")
        List<String> failureRedirectUris = (List<String>) ReflectionTestUtils.getField(failureHandler, "authorizedRedirectUris");

        assertEquals("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", secret);
        assertEquals(86400000L, expiration);
        assertEquals(604800000L, refreshExpiration);

        assertNotNull(successRedirectUris);
        assertFalse(successRedirectUris.isEmpty());
        assertTrue(successRedirectUris.contains("http://localhost:3000/oauth2/redirect"));

        assertNotNull(failureRedirectUris);
        assertFalse(failureRedirectUris.isEmpty());
        assertTrue(failureRedirectUris.contains("http://localhost:3000/oauth2/redirect"));

        // Multi-tenancy check
        assertEquals(TenantContext.DEFAULT_TENANT_ID, tenantIdentifierResolver.resolveCurrentTenantIdentifier());
    }
}

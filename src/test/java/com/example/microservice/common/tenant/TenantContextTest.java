package com.example.microservice.common.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @BeforeEach
    @AfterEach
    void cleanUp() {
        TenantContext.clear();
    }

    @Test
    void testDefaultTenantWhenNoneSet() {
        assertEquals(TenantContext.DEFAULT_TENANT_ID, TenantContext.getTenantId());
    }

    @Test
    void testSetAndGetTenantId() {
        TenantContext.setTenantId("tenant-alpha");
        assertEquals("tenant-alpha", TenantContext.getTenantId());
    }

    @Test
    void testSetNullOrBlankFallsBackToDefault() {
        TenantContext.setTenantId(null);
        assertEquals(TenantContext.DEFAULT_TENANT_ID, TenantContext.getTenantId());

        TenantContext.setTenantId("  ");
        assertEquals(TenantContext.DEFAULT_TENANT_ID, TenantContext.getTenantId());
    }

    @Test
    void testClearContext() {
        TenantContext.setTenantId("tenant-beta");
        assertEquals("tenant-beta", TenantContext.getTenantId());

        TenantContext.clear();
        assertEquals(TenantContext.DEFAULT_TENANT_ID, TenantContext.getTenantId());
    }
}

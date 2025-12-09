package com.example.productservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantProviderTest {

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @Test
    void testGetCurrentTenantIdWhenSet() {
        TenantContext.setTenantId("test-tenant");

        String tenantId = TenantProvider.getCurrentTenantId();

        assertEquals("test-tenant", tenantId);
    }

    @Test
    void testGetCurrentTenantIdThrowsExceptionWhenNotSet() {
        assertThrows(IllegalStateException.class, TenantProvider::getCurrentTenantId);
    }

    @Test
    void testGetCurrentTenantIdThrowsExceptionWhenBlank() {
        TenantContext.setTenantId("   ");

        assertThrows(IllegalStateException.class, TenantProvider::getCurrentTenantId);
    }

    @Test
    void testGetTenantIdOrNullWhenSet() {
        TenantContext.setTenantId("another-tenant");

        String tenantId = TenantProvider.getTenantIdOrNull();

        assertEquals("another-tenant", tenantId);
    }

    @Test
    void testGetTenantIdOrNullWhenNotSet() {
        String tenantId = TenantProvider.getTenantIdOrNull();

        assertNull(tenantId);
    }
}

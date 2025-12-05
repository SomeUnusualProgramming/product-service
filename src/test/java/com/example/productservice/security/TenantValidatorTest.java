package com.example.productservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantValidatorTest {

    private TenantValidator tenantValidator;

    @BeforeEach
    void setUp() {
        tenantValidator = new TenantValidator();
    }

    @Test
    void testValidTenantId() {
        assertTrue(tenantValidator.isValidTenant("tenant-1"));
        assertTrue(tenantValidator.isValidTenant("TENANT_A"));
        assertTrue(tenantValidator.isValidTenant("tenant123"));
        assertTrue(tenantValidator.isValidTenant("a"));
    }

    @Test
    void testInvalidTenantIdNull() {
        assertFalse(tenantValidator.isValidTenant(null));
    }

    @Test
    void testInvalidTenantIdBlank() {
        assertFalse(tenantValidator.isValidTenant(""));
        assertFalse(tenantValidator.isValidTenant("   "));
    }

    @Test
    void testInvalidTenantIdSpecialCharacters() {
        assertFalse(tenantValidator.isValidTenant("tenant@1"));
        assertFalse(tenantValidator.isValidTenant("tenant#2"));
        assertFalse(tenantValidator.isValidTenant("tenant.1"));
    }

    @Test
    void testInvalidTenantIdTooLong() {
        String longTenant = "a".repeat(51);
        assertFalse(tenantValidator.isValidTenant(longTenant));
    }

    @Test
    void testValidateTenantIdThrowsExceptionForInvalid() {
        assertThrows(IllegalArgumentException.class, () -> tenantValidator.validateTenantId(null));
        assertThrows(IllegalArgumentException.class, () -> tenantValidator.validateTenantId(""));
        assertThrows(IllegalArgumentException.class, () -> tenantValidator.validateTenantId("tenant@invalid"));
    }

    @Test
    void testValidateTenantIdDoesNotThrowForValid() {
        assertDoesNotThrow(() -> tenantValidator.validateTenantId("valid-tenant"));
        assertDoesNotThrow(() -> tenantValidator.validateTenantId("VALID_TENANT_123"));
    }
}

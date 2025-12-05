package com.example.productservice.security;

public final class TenantProvider {

    private TenantProvider() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static String getCurrentTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Tenant ID is not set in context. Make sure X-Tenant-Id header is provided.");
        }
        return tenantId;
    }

    public static String getTenantIdOrNull() {
        return TenantContext.getTenantId();
    }
}

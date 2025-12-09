package com.example.productservice.security;

public final class TenantContext {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static void setTenantId(String tenantId) {
        if (tenantId != null) {
            TENANT_ID.set(tenantId);
        }
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}

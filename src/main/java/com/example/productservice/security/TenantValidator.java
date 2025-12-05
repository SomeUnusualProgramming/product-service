package com.example.productservice.security;

import org.springframework.stereotype.Component;

@Component
public class TenantValidator {

    public boolean isValidTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return false;
        }
        
        return tenantId.matches("[a-zA-Z0-9_-]{1,50}");
    }

    public void validateTenantId(String tenantId) {
        if (!isValidTenant(tenantId)) {
            throw new IllegalArgumentException(
                "Invalid tenant ID format. Must be alphanumeric with optional hyphens and underscores, max 50 chars."
            );
        }
    }
}

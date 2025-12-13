package com.example.productservice.security;

import org.springframework.stereotype.Component;

/**
 * Validator component for tenant identification and validation.
 * Provides methods to verify tenant ID format compliance.
 */
@Component
public class TenantValidator {

    /**
     * Checks if the provided tenant ID is valid.
     * A valid tenant ID must be non-blank and match the alphanumeric pattern with optional
     * hyphens and underscores, with a maximum length of 50 characters.
     *
     * @param tenantId the tenant ID to validate
     * @return {@code true} if the tenant ID is valid, {@code false} otherwise
     */
    public boolean isValidTenant(final String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return false;
        }

        return tenantId.matches("[a-zA-Z0-9_-]{1,50}");
    }

    /**
     * Validates the tenant ID format and throws an exception if invalid.
     * A valid tenant ID must be non-blank and match the alphanumeric pattern with optional
     * hyphens and underscores, with a maximum length of 50 characters.
     *
     * @param tenantId the tenant ID to validate
     * @throws IllegalArgumentException if the tenant ID format is invalid
     */
    public void validateTenantId(final String tenantId) {
        if (!isValidTenant(tenantId)) {
            throw new IllegalArgumentException(
                    "Invalid tenant ID format. Must be alphanumeric with optional hyphens and "
                            + "underscores, max 50 chars."
            );
        }
    }
}

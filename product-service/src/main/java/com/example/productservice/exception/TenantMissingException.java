package com.example.productservice.exception;

public class TenantMissingException extends RuntimeException {
    public TenantMissingException() {
        super("Tenant ID is required. Please provide X-Tenant-Id header.");
    }

    public TenantMissingException(String message) {
        super(message);
    }

    public TenantMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}

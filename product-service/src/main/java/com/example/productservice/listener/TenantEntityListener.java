package com.example.productservice.listener;

import com.example.productservice.model.HasTenantId;
import com.example.productservice.security.TenantContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

@Component
public class TenantEntityListener {
    @PrePersist
    @PreUpdate
    public void setTenantId(HasTenantId entity) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null && entity.getTenantId() == null) {
            entity.setTenantId(tenantId);
        }
    }
}

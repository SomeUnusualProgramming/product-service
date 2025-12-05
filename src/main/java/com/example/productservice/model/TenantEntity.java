package com.example.productservice.model;

import com.example.productservice.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(TenantEntityListener.class)
public abstract class TenantEntity implements HasTenantId {
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
}

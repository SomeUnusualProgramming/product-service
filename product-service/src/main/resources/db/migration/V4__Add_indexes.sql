-- Add indexes for query optimization
CREATE INDEX IF NOT EXISTS idx_product_tenant_id ON product(tenant_id);
CREATE INDEX IF NOT EXISTS idx_product_tenant_event ON product(tenant_id, event_type);
CREATE INDEX IF NOT EXISTS idx_product_original_id ON product(original_product_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_product_event_time ON product(event_time DESC);
CREATE INDEX IF NOT EXISTS idx_product_tenant_created ON product(tenant_id, created_at DESC);

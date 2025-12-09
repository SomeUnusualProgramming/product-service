CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    stock_quantity INTEGER NOT NULL,
    original_product_id BIGINT,
    event_type VARCHAR(50),
    event_time TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL
);

CREATE INDEX idx_product_tenant_id ON product(tenant_id);
CREATE INDEX idx_product_original_product_id_tenant_id ON product(original_product_id, tenant_id);
CREATE INDEX idx_product_tenant_id_event_type ON product(tenant_id, event_type);

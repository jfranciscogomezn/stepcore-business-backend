CREATE TABLE clients (
    id              BIGSERIAL       PRIMARY KEY,
    tenant_id       BIGINT          NOT NULL,
    name            VARCHAR(150)    NOT NULL,
    tax_id          VARCHAR(30),
    contact_name    VARCHAR(150),
    contact_email   VARCHAR(255),
    contact_phone   VARCHAR(30),
    internal_notes  TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                        CONSTRAINT chk_client_status CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_client_tenant_name
    ON clients (tenant_id, lower(name));
CREATE INDEX idx_client_tenant_status
    ON clients (tenant_id, status);

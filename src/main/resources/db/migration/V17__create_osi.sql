CREATE TABLE osi (
    id                      BIGSERIAL       PRIMARY KEY,
    tenant_id               BIGINT          NOT NULL,
    osi_number              VARCHAR(30)     NOT NULL,
    client_id               BIGINT          NOT NULL REFERENCES clients(id),
    origin                  VARCHAR(300)    NOT NULL,
    destination             VARCHAR(300)    NOT NULL,
    load_window_start       TIMESTAMPTZ,
    load_window_end         TIMESTAMPTZ,
    delivery_window_start   TIMESTAMPTZ,
    delivery_window_end     TIMESTAMPTZ,
    commercial_reference    VARCHAR(150),
    internal_notes          TEXT,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'DRAFT'
                                CONSTRAINT chk_osi_status CHECK (status IN ('DRAFT','ACTIVE','CLOSED')),
    coordinator_user_id     BIGINT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    closed_at               TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_osi_tenant_number ON osi (tenant_id, osi_number);
CREATE INDEX idx_osi_tenant_status        ON osi (tenant_id, status);
CREATE INDEX idx_osi_tenant_created       ON osi (tenant_id, created_at DESC);
CREATE INDEX idx_osi_client               ON osi (client_id);

CREATE TABLE osi_transport_documents (
    id              BIGSERIAL       PRIMARY KEY,
    tenant_id       BIGINT          NOT NULL,
    osi_id          BIGINT          NOT NULL REFERENCES osi(id),
    vehicle_id      BIGINT          NOT NULL REFERENCES vehicles(id),
    type            VARCHAR(50)     NOT NULL DEFAULT 'otro',
    document_number VARCHAR(100),
    document_date   DATE,
    adjunct_uri     VARCHAR(500),
    internal_notes  TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otd_osi     ON osi_transport_documents (osi_id, vehicle_id);
CREATE INDEX idx_otd_tenant  ON osi_transport_documents (tenant_id);

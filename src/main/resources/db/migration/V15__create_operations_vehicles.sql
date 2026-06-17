CREATE TABLE vehicles (
    id              BIGSERIAL       PRIMARY KEY,
    tenant_id       BIGINT          NOT NULL,
    plate           VARCHAR(10)     NOT NULL,
    type            VARCHAR(30)     NOT NULL DEFAULT 'CAMION'
                        CONSTRAINT chk_vehicle_type CHECK (type IN ('CAMION','TRACTOMULA','FURGON','VAN','OTRO')),
    brand           VARCHAR(80),
    model           VARCHAR(80),
    year            SMALLINT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                        CONSTRAINT chk_vehicle_status CHECK (status IN ('ACTIVE','MAINTENANCE','RETIRED')),
    internal_notes  TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_vehicle_tenant_plate
    ON vehicles (tenant_id, plate);
CREATE INDEX idx_vehicle_tenant_status
    ON vehicles (tenant_id, status);

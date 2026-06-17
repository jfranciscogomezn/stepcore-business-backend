CREATE TABLE osi_vehicle_assignments (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           BIGINT          NOT NULL,
    osi_id              BIGINT          NOT NULL REFERENCES osi(id),
    vehicle_id          BIGINT          NOT NULL REFERENCES vehicles(id),
    state               VARCHAR(30)     NOT NULL DEFAULT 'PLANNED'
                            CONSTRAINT chk_ova_state CHECK (state IN (
                                'PLANNED','EN_RUTA','EN_DESTINO','DESCARGANDO',
                                'CERRADO_TRACKING','INCIDENTE')),
    assigned_user_ids   JSONB           NOT NULL DEFAULT '[]'::jsonb,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ova_osi        ON osi_vehicle_assignments (osi_id);
CREATE INDEX idx_ova_vehicle    ON osi_vehicle_assignments (vehicle_id);
CREATE INDEX idx_ova_tenant     ON osi_vehicle_assignments (tenant_id);

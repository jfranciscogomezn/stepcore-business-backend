CREATE TABLE osi_events (
    id                      BIGSERIAL       PRIMARY KEY,
    tenant_id               BIGINT          NOT NULL,
    osi_id                  BIGINT          NOT NULL REFERENCES osi(id),
    vehicle_id              BIGINT          NOT NULL REFERENCES vehicles(id),
    event_type_id           BIGINT          NOT NULL REFERENCES event_types(id),
    author_user_id          BIGINT          NOT NULL,
    text                    VARCHAR(2000)   NOT NULL,
    captured_at_local       TIMESTAMPTZ,
    received_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    geo_lat                 DECIMAL(9,6),
    geo_lng                 DECIMAL(9,6),
    effective_visibility    VARCHAR(30)     NOT NULL
                                CONSTRAINT chk_oe_visibility CHECK (effective_visibility IN (
                                    'INTERNO','CLIENTE','CLIENTE_CON_APROBACION','PENDIENTE_APROBACION')),
    parent_event_id         BIGINT          REFERENCES osi_events(id),
    correction_reason       TEXT,
    idempotency_key         UUID            NOT NULL,
    external_party_name     VARCHAR(150),
    external_party_document VARCHAR(50)
);

CREATE INDEX idx_oe_osi_vehicle  ON osi_events (osi_id, vehicle_id, received_at DESC);
CREATE INDEX idx_oe_tenant       ON osi_events (tenant_id, received_at DESC);
CREATE INDEX idx_oe_parent       ON osi_events (parent_event_id) WHERE parent_event_id IS NOT NULL;

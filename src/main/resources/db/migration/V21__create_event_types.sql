CREATE TABLE event_types (
    id                      BIGSERIAL       PRIMARY KEY,
    tenant_id               BIGINT          NOT NULL,
    name                    VARCHAR(100)    NOT NULL,
    description             TEXT,
    default_visibility      VARCHAR(30)     NOT NULL DEFAULT 'INTERNO'
                                CONSTRAINT chk_et_visibility CHECK (default_visibility IN (
                                    'INTERNO','CLIENTE','CLIENTE_CON_APROBACION')),
    min_attachments         SMALLINT        NOT NULL DEFAULT 0,
    max_attachments         SMALLINT        NOT NULL DEFAULT 10,
    has_measurement_form    BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_et_attachments CHECK (min_attachments >= 0 AND max_attachments >= min_attachments)
);

CREATE INDEX idx_et_tenant ON event_types (tenant_id);

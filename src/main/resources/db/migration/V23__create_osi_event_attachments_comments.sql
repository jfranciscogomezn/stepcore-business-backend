CREATE TABLE osi_event_attachments (
    id              BIGSERIAL       PRIMARY KEY,
    tenant_id       BIGINT          NOT NULL,
    event_id        BIGINT          NOT NULL REFERENCES osi_events(id),
    filename        VARCHAR(255)    NOT NULL,
    uri             VARCHAR(500)    NOT NULL,
    mime_type       VARCHAR(100),
    file_size_bytes BIGINT,
    checksum_sha256 VARCHAR(64),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_oea_event   ON osi_event_attachments (event_id);
CREATE INDEX idx_oea_tenant  ON osi_event_attachments (tenant_id);

CREATE TABLE osi_event_comments (
    id              BIGSERIAL       PRIMARY KEY,
    tenant_id       BIGINT          NOT NULL,
    event_id        BIGINT          NOT NULL REFERENCES osi_events(id),
    author_user_id  BIGINT          NOT NULL,
    text            VARCHAR(2000)   NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_oec_event   ON osi_event_comments (event_id);
CREATE INDEX idx_oec_tenant  ON osi_event_comments (tenant_id);

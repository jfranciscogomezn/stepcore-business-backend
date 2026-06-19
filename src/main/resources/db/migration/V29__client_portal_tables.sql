-- Tracking tokens: one active token per OSI for the client portal.
CREATE TABLE osi_tracking_tokens (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT        NOT NULL,
    osi_id              BIGINT        NOT NULL,
    token               UUID          NOT NULL UNIQUE,
    created_by_user_id  BIGINT,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    revoked_at          TIMESTAMPTZ
);

-- Enforce only one active (non-revoked) token per OSI.
CREATE UNIQUE INDEX idx_osi_tracking_tokens_active
    ON osi_tracking_tokens (osi_id)
    WHERE revoked_at IS NULL;

-- Portal access log: append-only, stores hashed IP for privacy.
CREATE TABLE osi_portal_access_log (
    id          BIGSERIAL PRIMARY KEY,
    token_id    BIGINT      NOT NULL REFERENCES osi_tracking_tokens (id),
    accessed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ip_hash     CHAR(64)    NOT NULL
);

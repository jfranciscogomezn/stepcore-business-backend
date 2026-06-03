-- Shared-database compatibility: audit_logs is owned by the security service in production.
-- Business integration tests need the same shape when the table is absent.

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    user_id     BIGINT,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   VARCHAR(100),
    old_value   TEXT,
    new_value   TEXT,
    details     TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_tenant_created ON audit_logs (tenant_id, created_at DESC);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'stepcore_app') THEN
        GRANT SELECT, INSERT ON audit_logs TO stepcore_app;
    END IF;
END $$;

CREATE TABLE employee_notifications (
    id                  BIGSERIAL       PRIMARY KEY,
    tenant_id           BIGINT          NOT NULL,
    recipient_user_id   BIGINT          NOT NULL,
    notification_type   VARCHAR(60)     NOT NULL,
    title               TEXT            NOT NULL,
    message             TEXT            NOT NULL,
    read                BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_en_recipient ON employee_notifications (recipient_user_id, created_at DESC);
CREATE INDEX idx_en_tenant     ON employee_notifications (tenant_id, created_at DESC);

GRANT SELECT, INSERT, UPDATE ON employee_notifications TO stepcore_app;

ALTER TABLE employee_notifications ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_employee_notifications ON employee_notifications
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

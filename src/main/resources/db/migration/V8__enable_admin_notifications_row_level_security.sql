GRANT SELECT, INSERT ON admin_notifications TO stepcore_app;

ALTER TABLE admin_notifications ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_admin_notifications ON admin_notifications
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

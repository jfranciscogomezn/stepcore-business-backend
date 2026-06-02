GRANT SELECT, INSERT, UPDATE, DELETE ON time_records TO stepcore_app;

ALTER TABLE time_records ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_time_records ON time_records
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

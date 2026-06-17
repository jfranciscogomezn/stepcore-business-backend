GRANT SELECT, INSERT, UPDATE ON time_correction_requests TO stepcore_app;

ALTER TABLE time_correction_requests ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_time_correction_requests ON time_correction_requests
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

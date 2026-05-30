-- RLS for business-owned tables on the shared database.
-- Reuses the stepcore_app runtime role when it already exists (created by security migrations).

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'stepcore_app') THEN
        CREATE ROLE stepcore_app LOGIN PASSWORD 'stepcore_app_pass';
        GRANT USAGE ON SCHEMA public TO stepcore_app;
        GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO stepcore_app;
        GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO stepcore_app;
        ALTER DEFAULT PRIVILEGES IN SCHEMA public
            GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO stepcore_app;
        ALTER DEFAULT PRIVILEGES IN SCHEMA public
            GRANT USAGE, SELECT ON SEQUENCES TO stepcore_app;
    END IF;
END $$;

GRANT SELECT, INSERT, UPDATE, DELETE ON payroll_configs TO stepcore_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON holidays TO stepcore_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO stepcore_app;

ALTER TABLE payroll_configs ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_payroll_configs ON payroll_configs
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

ALTER TABLE holidays ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_holidays ON holidays
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

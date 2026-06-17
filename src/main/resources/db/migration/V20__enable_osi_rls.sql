GRANT SELECT, INSERT, UPDATE ON osi TO stepcore_app;
ALTER TABLE osi ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_osi ON osi
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

GRANT SELECT, INSERT, UPDATE ON osi_vehicle_assignments TO stepcore_app;
ALTER TABLE osi_vehicle_assignments ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_osi_vehicle_assignments ON osi_vehicle_assignments
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

GRANT SELECT, INSERT, UPDATE ON osi_transport_documents TO stepcore_app;
ALTER TABLE osi_transport_documents ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_osi_transport_documents ON osi_transport_documents
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

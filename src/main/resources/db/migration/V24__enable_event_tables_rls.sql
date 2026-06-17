GRANT SELECT, INSERT, UPDATE ON event_types TO stepcore_app;
ALTER TABLE event_types ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_event_types ON event_types
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

GRANT SELECT, INSERT, UPDATE ON osi_events TO stepcore_app;
ALTER TABLE osi_events ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_osi_events ON osi_events
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

GRANT SELECT, INSERT, UPDATE ON osi_event_attachments TO stepcore_app;
ALTER TABLE osi_event_attachments ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_osi_event_attachments ON osi_event_attachments
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

GRANT SELECT, INSERT, UPDATE ON osi_event_comments TO stepcore_app;
ALTER TABLE osi_event_comments ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_osi_event_comments ON osi_event_comments
    USING (tenant_id = current_setting('app.current_tenant', true)::bigint)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::bigint);

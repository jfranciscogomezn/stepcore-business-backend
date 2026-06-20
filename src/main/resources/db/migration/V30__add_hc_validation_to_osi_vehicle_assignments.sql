-- HC documentary validation fields for each OSI-vehicle assignment.
ALTER TABLE osi_vehicle_assignments
    ADD COLUMN hc_validation_status     VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
        CONSTRAINT chk_hc_status CHECK (hc_validation_status IN ('PENDIENTE','VALIDADO','RECHAZADO')),
    ADD COLUMN hc_validation_notes      TEXT,
    ADD COLUMN hc_validated_by_user_id  BIGINT,
    ADD COLUMN hc_validated_at          TIMESTAMPTZ;

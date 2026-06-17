CREATE TABLE time_correction_requests (
    id              BIGSERIAL       PRIMARY KEY,
    tenant_id       BIGINT          NOT NULL,
    time_record_id  BIGINT          NOT NULL REFERENCES time_records(id),
    employee_id     BIGINT          NOT NULL REFERENCES employees(id),
    note            TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    resolution_note TEXT,
    resolved_by     BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMPTZ,
    CONSTRAINT chk_tcr_status CHECK (status IN ('PENDING','RESOLVED','DISMISSED'))
);

-- Only one PENDING request allowed per time record (partial unique index)
CREATE UNIQUE INDEX uq_tcr_pending_per_record
    ON time_correction_requests (time_record_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_tcr_tenant_status ON time_correction_requests (tenant_id, status);
CREATE INDEX idx_tcr_employee      ON time_correction_requests (employee_id);

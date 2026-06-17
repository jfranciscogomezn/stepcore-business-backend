-- Unique index for server-side idempotency deduplication (§3.6.1).
-- The combination (tenant_id, osi_id, vehicle_id, idempotency_key) identifies a unique event submission.
CREATE UNIQUE INDEX uq_oe_idempotency
    ON osi_events (tenant_id, osi_id, vehicle_id, idempotency_key);

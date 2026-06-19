-- Add manual GPS reference fields to osi_vehicle_assignments.
ALTER TABLE osi_vehicle_assignments
    ADD COLUMN gps_provider     VARCHAR(100),
    ADD COLUMN gps_reference_url VARCHAR(500);

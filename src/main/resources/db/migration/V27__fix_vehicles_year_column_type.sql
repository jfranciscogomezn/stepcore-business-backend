-- Correct vehicles.year column type: SMALLINT → INTEGER for Hibernate schema validation.
ALTER TABLE vehicles
    ALTER COLUMN year TYPE INTEGER;

-- Hibernate maps String(@Column length=64) to VARCHAR; the original V29 used CHAR(64)
-- (PostgreSQL bpchar). This migration aligns the DB type with the entity mapping.
-- CHAR and VARCHAR are functionally identical for a fixed-length SHA-256 hex hash;
-- no data is lost.
ALTER TABLE osi_portal_access_log
    ALTER COLUMN ip_hash TYPE VARCHAR(64);

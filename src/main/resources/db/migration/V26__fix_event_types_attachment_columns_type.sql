-- Correct column types: SMALLINT → INTEGER so Hibernate schema validation passes.
ALTER TABLE event_types
    ALTER COLUMN min_attachments TYPE INTEGER,
    ALTER COLUMN max_attachments TYPE INTEGER;

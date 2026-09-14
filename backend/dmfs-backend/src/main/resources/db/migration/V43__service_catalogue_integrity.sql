-- Prevent duplicate service names regardless of case or whitespace.
CREATE UNIQUE INDEX IF NOT EXISTS uq_service_catalogue_normalized_name
ON service_catalogue (LOWER(TRIM(name)));

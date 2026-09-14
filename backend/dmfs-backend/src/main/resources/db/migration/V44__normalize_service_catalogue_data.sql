-- Normalize legacy service personnel values.
UPDATE service_catalogue
SET required_personnel = 'DRONE_OPERATOR'
WHERE LOWER(TRIM(required_personnel)) = 'drone operator';

UPDATE service_catalogue
SET required_personnel = 'GEOLOGIST'
WHERE LOWER(TRIM(required_personnel)) = 'geologist';

-- Apply the normalized service-name uniqueness rule.
CREATE UNIQUE INDEX IF NOT EXISTS uq_service_catalogue_normalized_name
ON service_catalogue (LOWER(TRIM(name)));

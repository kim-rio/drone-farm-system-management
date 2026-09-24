-- ============================================================
-- V58: Scope service catalogue to subscriber company
-- ============================================================

ALTER TABLE service_catalogue
    ADD COLUMN company_id BIGINT;

-- Legacy catalogue rows belong to the original company in the
-- existing single-tenant data model. New rows are always assigned
-- from the authenticated user's company by the application.
UPDATE service_catalogue
SET company_id = 1
WHERE company_id IS NULL;

ALTER TABLE service_catalogue
    ADD CONSTRAINT fk_service_catalogue_company
    FOREIGN KEY (company_id)
    REFERENCES subscriber_companies(id)
    ON DELETE CASCADE;

ALTER TABLE service_catalogue
    ALTER COLUMN company_id SET NOT NULL;

CREATE INDEX idx_service_catalogue_company
    ON service_catalogue(company_id);

DROP INDEX IF EXISTS uq_service_catalogue_normalized_name;

CREATE UNIQUE INDEX uq_service_catalogue_company_normalized_name
    ON service_catalogue (company_id, LOWER(TRIM(name)));

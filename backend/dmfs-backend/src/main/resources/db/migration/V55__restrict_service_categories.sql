-- Normalize service categories to the two supported DMFS service domains.
UPDATE service_catalogue
SET category = CASE
    WHEN UPPER(TRIM(category)) IN ('AGRICULTURE', 'AGRICULTURAL') THEN 'AGRICULTURE'
    ELSE 'MINING'
END;

ALTER TABLE service_catalogue
    ADD CONSTRAINT chk_service_catalogue_category
    CHECK (category IN ('MINING', 'AGRICULTURE'));

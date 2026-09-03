ALTER TABLE subscriber_companies
    DROP COLUMN company_code,
    DROP COLUMN address;

ALTER TABLE subscriber_companies
    ADD COLUMN registration_number VARCHAR(100) NOT NULL,
    ADD COLUMN tin VARCHAR(100),
    ADD COLUMN country VARCHAR(100) NOT NULL,
    ADD COLUMN region VARCHAR(100) NOT NULL,
    ADD COLUMN city VARCHAR(100) NOT NULL,
    ADD COLUMN physical_address VARCHAR(255) NOT NULL;

ALTER TABLE subscriber_companies
    ADD CONSTRAINT uk_subscriber_companies_registration_number
    UNIQUE (registration_number);

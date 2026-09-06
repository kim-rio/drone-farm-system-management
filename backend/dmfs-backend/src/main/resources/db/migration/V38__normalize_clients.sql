-- ============================================================
-- V38: Normalize client data into client type profiles
-- ============================================================

CREATE TABLE client_companies (
    client_id BIGINT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100),
    tin VARCHAR(50),
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(255),

    CONSTRAINT fk_client_companies_client
        FOREIGN KEY (client_id)
        REFERENCES clients(id)
        ON DELETE CASCADE
);

CREATE TABLE client_individuals (
    client_id BIGINT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(255),

    CONSTRAINT fk_client_individuals_client
        FOREIGN KEY (client_id)
        REFERENCES clients(id)
        ON DELETE CASCADE
);

-- Move existing individual clients into the individual profile table.
INSERT INTO client_individuals (
    client_id,
    first_name,
    last_name,
    email,
    phone,
    address
)
SELECT
    id,
    first_name,
    last_name,
    email,
    phone,
    address
FROM clients
WHERE type = 'INDIVIDUAL';

-- Move existing exploration-company clients into the company profile table.
INSERT INTO client_companies (
    client_id,
    company_name,
    registration_number,
    tin,
    email,
    phone,
    address
)
SELECT
    id,
    company_name,
    NULL,
    tin,
    email,
    phone,
    address
FROM clients
WHERE type = 'EXPLORATION_COMPANY';

-- Rename the old type value to the new business terminology.
UPDATE clients
SET type = 'COMPANY'
WHERE type = 'EXPLORATION_COMPANY';

-- Remove subtype-specific data from the base client table.
ALTER TABLE clients
    DROP COLUMN company_name,
    DROP COLUMN first_name,
    DROP COLUMN last_name,
    DROP COLUMN email,
    DROP COLUMN phone,
    DROP COLUMN address,
    DROP COLUMN identification_number,
    DROP COLUMN tin;

-- Keep client type restricted to the two supported business types.
ALTER TABLE clients
    ADD CONSTRAINT chk_clients_type
    CHECK (type IN ('INDIVIDUAL', 'COMPANY'));

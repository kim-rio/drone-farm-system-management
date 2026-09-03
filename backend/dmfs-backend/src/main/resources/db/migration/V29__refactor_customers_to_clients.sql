-- ============================================================
-- V29: Refactor customers into clients
-- ============================================================

ALTER TABLE customers
    RENAME TO clients;

ALTER TABLE clients
    DROP CONSTRAINT IF EXISTS fk_customer_user;

ALTER TABLE clients
    DROP COLUMN IF EXISTS user_id;

ALTER INDEX IF EXISTS idx_customers_email
    RENAME TO idx_clients_email;

ALTER INDEX IF EXISTS idx_customers_code
    RENAME TO idx_clients_code;

ALTER INDEX IF EXISTS idx_customers_status
    RENAME TO idx_clients_status;

UPDATE clients
SET company_id = 1
WHERE company_id IS NULL;

ALTER TABLE clients
    ALTER COLUMN company_id SET NOT NULL;

ALTER TABLE clients
    ADD CONSTRAINT fk_clients_company
        FOREIGN KEY (company_id)
        REFERENCES subscriber_companies(id)
        ON DELETE CASCADE;

CREATE INDEX idx_clients_company_id
    ON clients(company_id);

ALTER TABLE clients
    ADD COLUMN registered_by BIGINT;

ALTER TABLE clients
    ADD CONSTRAINT fk_clients_registered_by
        FOREIGN KEY (registered_by)
        REFERENCES users(id)
        ON DELETE SET NULL;

CREATE INDEX idx_clients_registered_by
    ON clients(registered_by);

ALTER TABLE clients
    RENAME COLUMN customer_code TO client_code;

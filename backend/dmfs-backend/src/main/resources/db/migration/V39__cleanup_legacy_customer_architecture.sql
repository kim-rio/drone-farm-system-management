-- ============================================================
-- V39: Clean legacy customer/user remnants and normalize client ownership
-- ============================================================

-- ------------------------------------------------------------
-- 1. Remove legacy users that do not belong to the current
--    internal staff architecture.
--
--    SUPER_ADMIN remains company-less intentionally.
--    CUSTOMER accounts are no longer required because customers
--    are represented by the clients domain and do not log in.
-- ------------------------------------------------------------

DELETE FROM users
WHERE email = 'Admin@dmfs.com'
  AND role = 'MANAGEMENT'
  AND company_id IS NULL;

DELETE FROM users
WHERE role = 'CUSTOMER'
  AND company_id IS NULL;


-- ------------------------------------------------------------
-- 2. Remove the legacy foreign key inherited from the old
--    customers table.
--
--    V29 created the correct fk_clients_company but did not
--    remove the old fk_customers_company.
-- ------------------------------------------------------------

ALTER TABLE clients
    DROP CONSTRAINT IF EXISTS fk_customers_company;


-- ------------------------------------------------------------
-- 3. The application uses company-scoped client codes.
--
--    Remove the old global uniqueness constraint and replace
--    it with a company + client_code uniqueness constraint.
-- ------------------------------------------------------------

ALTER TABLE clients
    DROP CONSTRAINT IF EXISTS customers_customer_code_key;

ALTER TABLE clients
    ADD CONSTRAINT uk_clients_company_client_code
    UNIQUE (company_id, client_code);


-- ------------------------------------------------------------
-- 4. Keep the current client ownership relationship explicit.
-- ------------------------------------------------------------

ALTER TABLE clients
    DROP CONSTRAINT IF EXISTS fk_clients_company;

ALTER TABLE clients
    ADD CONSTRAINT fk_clients_company
        FOREIGN KEY (company_id)
        REFERENCES subscriber_companies(id)
        ON DELETE CASCADE;


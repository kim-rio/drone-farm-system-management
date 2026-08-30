-- V6: Replace legacy clients relationship with customers relationship

-- Remove the old foreign key from farms
ALTER TABLE farms
DROP CONSTRAINT IF EXISTS fk_farms_client;

-- Remove the legacy client_id column
ALTER TABLE farms
DROP COLUMN IF EXISTS client_id;

-- Make sure the customer relationship exists
ALTER TABLE farms
ADD CONSTRAINT fk_farms_customer
FOREIGN KEY (customer_id)
REFERENCES customers(id)
ON DELETE CASCADE;

-- Remove the obsolete clients table
DROP TABLE IF EXISTS clients;
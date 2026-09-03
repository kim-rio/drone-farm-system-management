-- ============================================================
-- V27: Create platform foundation
-- ============================================================

-- ============================================================
-- 1. SUBSCRIBER COMPANIES
-- ============================================================

CREATE TABLE subscriber_companies (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(150) NOT NULL,
    company_code VARCHAR(50) NOT NULL UNIQUE,

    email VARCHAR(100),
    phone VARCHAR(30),
    address VARCHAR(255),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_subscriber_companies_status
    ON subscriber_companies(status);

CREATE INDEX idx_subscriber_companies_name
    ON subscriber_companies(name);


-- ============================================================
-- 2. SUBSCRIPTION PLANS
-- ============================================================

CREATE TABLE subscription_plans (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),

    price NUMERIC(12, 2) NOT NULL DEFAULT 0,
    billing_cycle VARCHAR(30) NOT NULL,

    max_users INTEGER,
    max_customers INTEGER,
    max_farms INTEGER,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_subscription_plans_active
    ON subscription_plans(active);


-- ============================================================
-- 3. SUBSCRIPTIONS
-- ============================================================

CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,

    company_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_subscription_company
        FOREIGN KEY (company_id)
        REFERENCES subscriber_companies(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_subscription_plan
        FOREIGN KEY (plan_id)
        REFERENCES subscription_plans(id)
);

CREATE INDEX idx_subscriptions_company
    ON subscriptions(company_id);

CREATE INDEX idx_subscriptions_plan
    ON subscriptions(plan_id);

CREATE INDEX idx_subscriptions_status
    ON subscriptions(status);

CREATE INDEX idx_subscriptions_end_date
    ON subscriptions(end_date);


-- ============================================================
-- 4. ADD COMPANY TO USERS
-- ============================================================

ALTER TABLE users
ADD COLUMN company_id BIGINT;

ALTER TABLE users
ADD CONSTRAINT fk_users_company
    FOREIGN KEY (company_id)
    REFERENCES subscriber_companies(id)
    ON DELETE SET NULL;

CREATE INDEX idx_users_company_id
    ON users(company_id);


-- ============================================================
-- 5. ADD COMPANY TO CUSTOMERS
-- ============================================================

ALTER TABLE customers
ADD COLUMN company_id BIGINT;

ALTER TABLE customers
ADD CONSTRAINT fk_customers_company
    FOREIGN KEY (company_id)
    REFERENCES subscriber_companies(id)
    ON DELETE SET NULL;

CREATE INDEX idx_customers_company_id
    ON customers(company_id);
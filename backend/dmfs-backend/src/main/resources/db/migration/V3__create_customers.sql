CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,

    customer_code VARCHAR(30) NOT NULL UNIQUE,

    type VARCHAR(30) NOT NULL,

    company_name VARCHAR(150),

    first_name VARCHAR(100),

    last_name VARCHAR(100),

    email VARCHAR(100),

    phone VARCHAR(30),

    address VARCHAR(255),

    identification_number VARCHAR(100),

    tin VARCHAR(50),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    user_id BIGINT NOT NULL UNIQUE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    CONSTRAINT fk_customer_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_customers_email
    ON customers(email);

CREATE INDEX idx_customers_code
    ON customers(customer_code);

CREATE INDEX idx_customers_status
    ON customers(status);
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,

    service_request_id BIGINT NOT NULL,

    amount NUMERIC(14,2) NOT NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'TZS',

    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',

    provider VARCHAR(30) NOT NULL DEFAULT 'PESAPAL',

    merchant_reference VARCHAR(50) NOT NULL UNIQUE,

    provider_tracking_id VARCHAR(100),

    provider_confirmation_code VARCHAR(150),

    payment_method VARCHAR(50),

    payment_account VARCHAR(100),

    redirect_url TEXT,

    failure_reason TEXT,

    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    paid_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT fk_payments_service_request
        FOREIGN KEY (service_request_id)
        REFERENCES service_request(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_payments_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_payments_service_request
    ON payments(service_request_id);

CREATE INDEX idx_payments_status
    ON payments(status);

CREATE INDEX idx_payments_provider_tracking
    ON payments(provider_tracking_id);

CREATE INDEX idx_payments_created_at
    ON payments(created_at);

ALTER TABLE service_request
    ADD COLUMN control_number VARCHAR(50),
    ADD COLUMN amount NUMERIC(14,2),
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN paid_at TIMESTAMP;

CREATE INDEX idx_service_request_payment_status
    ON service_request(payment_status);

CREATE UNIQUE INDEX uq_service_request_control_number
    ON service_request(control_number)
    WHERE control_number IS NOT NULL;

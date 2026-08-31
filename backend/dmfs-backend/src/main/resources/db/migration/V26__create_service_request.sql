CREATE TABLE service_request (
    id BIGSERIAL PRIMARY KEY,

    customer_id BIGINT NOT NULL,

    farm_id BIGINT NOT NULL,

    farm_block_id BIGINT NOT NULL,

    service_catalogue_id BIGINT NOT NULL,

    requested_date DATE NOT NULL,

    notes TEXT,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    CONSTRAINT fk_service_request_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_service_request_farm
        FOREIGN KEY (farm_id)
        REFERENCES farms(id),

    CONSTRAINT fk_service_request_farm_block
        FOREIGN KEY (farm_block_id)
        REFERENCES farm_blocks(id),

    CONSTRAINT fk_service_request_catalogue
        FOREIGN KEY (service_catalogue_id)
        REFERENCES service_catalogue(id)
);

CREATE INDEX idx_service_request_customer
    ON service_request(customer_id);

CREATE INDEX idx_service_request_farm
    ON service_request(farm_id);

CREATE INDEX idx_service_request_farm_block
    ON service_request(farm_block_id);

CREATE INDEX idx_service_request_service_catalogue
    ON service_request(service_catalogue_id);

CREATE INDEX idx_service_request_status
    ON service_request(status);

CREATE INDEX idx_service_request_requested_date
    ON service_request(requested_date);
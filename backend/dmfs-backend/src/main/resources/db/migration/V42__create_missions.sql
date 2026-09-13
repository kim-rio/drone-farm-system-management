-- =========================================================
-- V40: Create missions table
-- =========================================================

CREATE TABLE missions (

    id BIGSERIAL PRIMARY KEY,

    mission_code VARCHAR(40) NOT NULL UNIQUE,

    company_id BIGINT NOT NULL,

    service_request_id BIGINT NOT NULL,

    customer_id BIGINT NOT NULL,

    farm_id BIGINT NOT NULL,

    farm_block_id BIGINT NOT NULL,

    operator_id BIGINT,

    drone_id BIGINT,

    scheduled_date DATE,

    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    notes TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    -- =====================================================
    -- FOREIGN KEYS
    -- =====================================================

    CONSTRAINT fk_missions_company
        FOREIGN KEY (company_id)
        REFERENCES subscriber_companies(id),

    CONSTRAINT fk_missions_service_request
        FOREIGN KEY (service_request_id)
        REFERENCES service_request(id),

    CONSTRAINT fk_missions_customer
        FOREIGN KEY (customer_id)
        REFERENCES clients(id),

    CONSTRAINT fk_missions_farm
        FOREIGN KEY (farm_id)
        REFERENCES farms(id),

    CONSTRAINT fk_missions_farm_block
        FOREIGN KEY (farm_block_id)
        REFERENCES farm_blocks(id),

    CONSTRAINT fk_missions_operator
        FOREIGN KEY (operator_id)
        REFERENCES users(id),

    CONSTRAINT fk_missions_drone
        FOREIGN KEY (drone_id)
        REFERENCES drones(id)
);


-- =========================================================
-- INDEXES
-- =========================================================

CREATE INDEX idx_missions_company_id
    ON missions(company_id);

CREATE INDEX idx_missions_service_request_id
    ON missions(service_request_id);

CREATE INDEX idx_missions_operator_id
    ON missions(operator_id);

CREATE INDEX idx_missions_drone_id
    ON missions(drone_id);

CREATE INDEX idx_missions_farm_id
    ON missions(farm_id);

CREATE INDEX idx_missions_block_id
    ON missions(farm_block_id);
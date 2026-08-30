CREATE TABLE service_catalogue (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(150) NOT NULL,

    category VARCHAR(50) NOT NULL,

    description TEXT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    unit_of_measurement VARCHAR(30) NOT NULL,

    standard_price NUMERIC(12,2) NOT NULL,

    minimum_area NUMERIC(12,2) NOT NULL,

    required_equipment TEXT,

    required_personnel TEXT,

    estimated_duration_minutes INTEGER NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    CONSTRAINT chk_service_catalogue_price
        CHECK (standard_price >= 0),

    CONSTRAINT chk_service_catalogue_minimum_area
        CHECK (minimum_area > 0),

    CONSTRAINT chk_service_catalogue_duration
        CHECK (estimated_duration_minutes > 0)
);

CREATE INDEX idx_service_catalogue_name
    ON service_catalogue(name);

CREATE INDEX idx_service_catalogue_category
    ON service_catalogue(category);

CREATE INDEX idx_service_catalogue_status
    ON service_catalogue(status);
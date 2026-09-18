-- ============================================================
-- V46: Create Survey Data Package and Survey Data File tables
-- ============================================================

CREATE TABLE survey_data_packages (
    id BIGSERIAL PRIMARY KEY,

    survey_id BIGINT NOT NULL,

    package_code VARCHAR(50) NOT NULL UNIQUE,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    submitted_by BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    validated_at TIMESTAMP,

    submitted_at TIMESTAMP,

    CONSTRAINT fk_survey_data_package_survey
        FOREIGN KEY (survey_id)
        REFERENCES survey(id),

    CONSTRAINT fk_survey_data_package_submitted_by
        FOREIGN KEY (submitted_by)
        REFERENCES users(id)
);


CREATE TABLE survey_data_files (
    id BIGSERIAL PRIMARY KEY,

    package_id BIGINT NOT NULL,

    file_name VARCHAR(255) NOT NULL,

    file_type VARCHAR(20) NOT NULL,

    file_extension VARCHAR(20) NOT NULL,

    file_size BIGINT NOT NULL,

    storage_key VARCHAR(500),

    checksum VARCHAR(64),

    validation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    validation_message TEXT,

    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_survey_data_file_package
        FOREIGN KEY (package_id)
        REFERENCES survey_data_packages(id)
        ON DELETE CASCADE
);


CREATE INDEX idx_survey_data_packages_survey_id
    ON survey_data_packages(survey_id);

CREATE INDEX idx_survey_data_files_package_id
    ON survey_data_files(package_id);
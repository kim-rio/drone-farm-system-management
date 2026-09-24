-- =========================================================
-- V59: Company-scoped numbering for service requests and missions
--
-- Each subscriber company gets its own independent counter:
--   Company A: SR-0001, SR-0002 ... / MIS-0001, MIS-0002 ...
--   Company B: SR-0001, SR-0002 ... / MIS-0001, MIS-0002 ...
--
-- The counters are persisted in a locked sequence table so concurrent
-- requests cannot receive the same number.
-- =========================================================

CREATE TABLE company_document_sequences (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    next_number BIGINT NOT NULL DEFAULT 1,

    CONSTRAINT fk_company_document_sequences_company
        FOREIGN KEY (company_id)
        REFERENCES subscriber_companies(id),

    CONSTRAINT uk_company_document_sequence
        UNIQUE (company_id, document_type),

    CONSTRAINT chk_company_document_sequence_next_number
        CHECK (next_number >= 1)
);

CREATE INDEX idx_company_document_sequence_company
    ON company_document_sequences(company_id);


-- =========================================================
-- SERVICE REQUEST NUMBER
-- =========================================================

ALTER TABLE service_request
    ADD COLUMN request_number VARCHAR(30);

-- Existing requests are numbered independently within their customer's company.
WITH ranked_requests AS (
    SELECT
        sr.id,
        c.company_id,
        ROW_NUMBER() OVER (
            PARTITION BY c.company_id
            ORDER BY sr.created_at ASC, sr.id ASC
        ) AS sequence_number
    FROM service_request sr
    JOIN clients c
        ON c.id = sr.customer_id
)
UPDATE service_request sr
SET request_number = 'SR-' || LPAD(r.sequence_number::TEXT, 4, '0')
FROM ranked_requests r
WHERE sr.id = r.id;

ALTER TABLE service_request
    ALTER COLUMN request_number SET NOT NULL;

CREATE INDEX idx_service_request_request_number
    ON service_request(request_number);


-- =========================================================
-- MISSION NUMBER
-- =========================================================

-- The old mission_code was globally unique. That prevented every company
-- from having its own MIS-0001, MIS-0002, ... numbering.
ALTER TABLE missions
    DROP CONSTRAINT IF EXISTS missions_mission_code_key;

WITH ranked_missions AS (
    SELECT
        m.id,
        m.company_id,
        ROW_NUMBER() OVER (
            PARTITION BY m.company_id
            ORDER BY m.created_at ASC, m.id ASC
        ) AS sequence_number
    FROM missions m
)
UPDATE missions m
SET mission_code = 'MIS-' || LPAD(r.sequence_number::TEXT, 4, '0')
FROM ranked_missions r
WHERE m.id = r.id;

ALTER TABLE missions
    ADD CONSTRAINT uk_missions_company_mission_code
    UNIQUE (company_id, mission_code);


-- =========================================================
-- SEED THE NEXT NUMBER FOR EACH EXISTING COMPANY
-- =========================================================

INSERT INTO company_document_sequences (
    company_id,
    document_type,
    next_number
)
SELECT
    c.id,
    'SERVICE_REQUEST',
    COALESCE(MAX(x.sequence_number), 0) + 1
FROM subscriber_companies c
LEFT JOIN (
    SELECT
        cl.company_id,
        ROW_NUMBER() OVER (
            PARTITION BY cl.company_id
            ORDER BY sr.created_at ASC, sr.id ASC
        ) AS sequence_number
    FROM service_request sr
    JOIN clients cl
        ON cl.id = sr.customer_id
) x
    ON x.company_id = c.id
GROUP BY c.id
ON CONFLICT (company_id, document_type) DO NOTHING;

INSERT INTO company_document_sequences (
    company_id,
    document_type,
    next_number
)
SELECT
    c.id,
    'MISSION',
    COALESCE(MAX(x.sequence_number), 0) + 1
FROM subscriber_companies c
LEFT JOIN (
    SELECT
        m.company_id,
        ROW_NUMBER() OVER (
            PARTITION BY m.company_id
            ORDER BY m.created_at ASC, m.id ASC
        ) AS sequence_number
    FROM missions m
) x
    ON x.company_id = c.id
GROUP BY c.id
ON CONFLICT (company_id, document_type) DO NOTHING;

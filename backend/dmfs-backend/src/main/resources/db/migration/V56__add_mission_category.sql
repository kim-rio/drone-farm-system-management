ALTER TABLE missions
    ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'MINING';

ALTER TABLE missions
    ADD CONSTRAINT chk_missions_category
    CHECK (category IN ('MINING', 'AGRICULTURE'));

CREATE INDEX idx_missions_category ON missions(category);

ALTER TABLE subscriber_companies
    ADD COLUMN IF NOT EXISTS workspace_slug VARCHAR(100);

UPDATE subscriber_companies
SET workspace_slug = 'company-' || id
WHERE workspace_slug IS NULL
   OR TRIM(workspace_slug) = '';

ALTER TABLE subscriber_companies
    ALTER COLUMN workspace_slug SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_subscriber_companies_workspace_slug
    ON subscriber_companies(workspace_slug);
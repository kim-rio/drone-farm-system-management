UPDATE subscriber_companies
SET workspace_slug = 'tukupala'
WHERE LOWER(name) = 'tukupala'
  AND workspace_slug = 'company-1';

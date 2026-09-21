CREATE TABLE client_invitations (
    id BIGSERIAL PRIMARY KEY,

    client_id BIGINT NOT NULL,

    email VARCHAR(100) NOT NULL,

    token_hash VARCHAR(128) NOT NULL UNIQUE,

    expires_at TIMESTAMP NOT NULL,

    used_at TIMESTAMP NULL,

    invited_by BIGINT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_client_invitations_client
        FOREIGN KEY (client_id)
        REFERENCES clients(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_client_invitations_user
        FOREIGN KEY (invited_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_client_invitations_client
    ON client_invitations(client_id);

CREATE INDEX idx_client_invitations_email
    ON client_invitations(email);

CREATE INDEX idx_client_invitations_expires
    ON client_invitations(expires_at);

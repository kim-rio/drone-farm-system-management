CREATE TABLE client_users (
    client_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_client_users
        PRIMARY KEY (client_id, user_id),

    CONSTRAINT fk_client_users_client
        FOREIGN KEY (client_id)
        REFERENCES clients(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_client_users_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_client_users_user
    ON client_users(user_id);

CREATE INDEX idx_client_users_client
    ON client_users(client_id);

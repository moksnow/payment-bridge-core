-- ============================================================
-- V2__add_users.sql
-- Add users table and link payments to users
-- ============================================================

CREATE TABLE users
(
    id            VARCHAR(36) PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE payments
    ADD COLUMN user_id VARCHAR(36) REFERENCES users (id);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_payments_user_id ON payments (user_id);

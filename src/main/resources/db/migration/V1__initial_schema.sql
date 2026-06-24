-- ============================================================
-- V1__initial_schema.sql
-- Payment Bridge - Initial Schema (H2)
-- ============================================================

CREATE TABLE users (
    id            VARCHAR(36)  PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
                          id               VARCHAR(36) PRIMARY KEY,
    user_id                 VARCHAR(36)    REFERENCES users(id),
                          idempotency_key  VARCHAR(100)    NOT NULL UNIQUE,
    sender_wallet_account   VARCHAR(100)   NOT NULL,
    receiver_wallet_account VARCHAR(100)   NOT NULL,
                          amount           DECIMAL(30, 10) NOT NULL,
                          currency         VARCHAR(10)     NOT NULL,
                          receive_amount   DECIMAL(30, 10),
                          receive_currency VARCHAR(10),
                          fx_rate          DECIMAL(20, 10),
                          rail_type        VARCHAR(30)     NOT NULL,
                          status           VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
                          description      VARCHAR(500),
                          failure_reason   VARCHAR(500),
                          external_ref     VARCHAR(255),
                          created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE TABLE wallets (
    id                   VARCHAR(36)  PRIMARY KEY,
    user_id              VARCHAR(36)  NOT NULL REFERENCES users(id),
    currency             VARCHAR(10)  NOT NULL,
    ledger_account_code  VARCHAR(100) NOT NULL UNIQUE,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_wallet_user_currency UNIQUE (user_id, currency)
);

CREATE TABLE ledger_entries (
    id              VARCHAR(36)    PRIMARY KEY,
    payment_id      VARCHAR(36),
    reference_type  VARCHAR(20)    NOT NULL DEFAULT 'PAYMENT',
    account_code    VARCHAR(100)   NOT NULL,
    entry_type      VARCHAR(10)    NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount          DECIMAL(30,10) NOT NULL,
    currency        VARCHAR(10)    NOT NULL,
    description     VARCHAR(500),
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ledger_amount CHECK (amount > 0)
);

CREATE TABLE kyc_profiles (
    id           VARCHAR(36)  PRIMARY KEY,
    user_id      VARCHAR(36)  NOT NULL UNIQUE REFERENCES users(id),
    kyc_level    VARCHAR(20)  NOT NULL DEFAULT 'UNVERIFIED',
    reviewed_by  VARCHAR(100),
    notes        VARCHAR(500),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE aml_flags (
    id          VARCHAR(36)    PRIMARY KEY,
    payment_id  VARCHAR(36)    NOT NULL,
    user_id     VARCHAR(36)    NOT NULL REFERENCES users(id),
    status      VARCHAR(20)    NOT NULL,
    reason      VARCHAR(500)   NOT NULL,
    amount      DECIMAL(30,10),
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email             ON users(email);
CREATE INDEX idx_payments_status         ON payments(status);
CREATE INDEX idx_payments_idempotency    ON payments(idempotency_key);
CREATE INDEX idx_payments_user_id        ON payments(user_id);
CREATE INDEX idx_wallets_user_id         ON wallets(user_id);
CREATE INDEX idx_wallets_ledger_account  ON wallets(ledger_account_code);
CREATE INDEX idx_ledger_payment_id       ON ledger_entries(payment_id);
CREATE INDEX idx_ledger_account_code     ON ledger_entries(account_code);
CREATE INDEX idx_ledger_reference_type   ON ledger_entries(reference_type);
CREATE INDEX idx_kyc_user_id             ON kyc_profiles(user_id);
CREATE INDEX idx_aml_user_id             ON aml_flags(user_id);
CREATE INDEX idx_aml_status              ON aml_flags(status);
CREATE INDEX idx_aml_created_at          ON aml_flags(created_at);

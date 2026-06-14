-- ============================================================
-- V1__initial_schema.sql
-- Payment Bridge MVP - Initial Schema (H2)
-- ============================================================

CREATE TABLE payments (
    id               VARCHAR(36)    PRIMARY KEY,
    idempotency_key  VARCHAR(100)   NOT NULL UNIQUE,
    sender_account   VARCHAR(100)   NOT NULL,
    receiver_account VARCHAR(100)   NOT NULL,
    amount           DECIMAL(30,10) NOT NULL,
    currency         VARCHAR(10)    NOT NULL,
    rail_type        VARCHAR(30)    NOT NULL,
    status           VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    description      VARCHAR(500),
    failure_reason   VARCHAR(500),
    external_ref     VARCHAR(255),
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE TABLE ledger_entries (
    id              VARCHAR(36)    PRIMARY KEY,
    payment_id      VARCHAR(36)    NOT NULL REFERENCES payments(id),
    account_code    VARCHAR(100)   NOT NULL,
    entry_type      VARCHAR(10)    NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount          DECIMAL(30,10) NOT NULL,
    currency        VARCHAR(10)    NOT NULL,
    description     VARCHAR(500),
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ledger_amount CHECK (amount > 0)
);

CREATE INDEX idx_payments_status         ON payments(status);
CREATE INDEX idx_payments_idempotency    ON payments(idempotency_key);
CREATE INDEX idx_ledger_payment_id       ON ledger_entries(payment_id);
CREATE INDEX idx_ledger_account_code     ON ledger_entries(account_code);

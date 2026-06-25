# Payment Bridge Core

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/badge/Build-Active-orange.svg)]()
[![License](https://img.shields.io/badge/License-Internal-lightgrey.svg)]()

> A backend-first fintech infrastructure prototype exploring payments, wallets, ledgers, FX conversion, KYC/AML compliance, and CBDC bridge infrastructure.

---

## Overview

Payment Bridge Core is a fintech infrastructure prototype that models how modern payment systems work under the hood — from JWT auth and wallet management through FX conversion, compliance rules, and CBDC settlement via ISO 20022 messaging.

The goal is not to build a production bank.

The goal is to understand the engineering concepts, system boundaries, and architectural decisions that power modern financial infrastructure — including the emerging CBDC ecosystem.

---

## Why This Project Exists

Most developers integrate payment APIs without seeing the infrastructure that sits behind them.

This project explores:

- payment processing workflows and lifecycle
- wallet and balance management (ledger-backed)
- double-entry ledger recording
- payment rail abstraction
- FX conversion and multi-currency support
- KYC level enforcement and AML pattern detection
- CBDC bridge: MINT, REDEEM, TRANSFER, SWAP operations
- ISO 20022 messaging standard (pacs.008 / pacs.002)

---

## Tech Stack

| Category | Technology            |
|---------------|-------------------------|
| Language | Java 21               |
| Framework | Spring Boot 3.5.x |
| Security | Spring Security + JWT |
| Database      | H2 (in-memory)          |
| Migration | Flyway                |
| Documentation | OpenAPI / Swagger     |
| Testing | JUnit 5, Mockito      |

---

## Current Architecture

```text
User
 │
 ▼
Auth & JWT
 │
 ▼
Wallet (balance from ledger)
 │
 ▼
Payment Service
 │
 ├── KYC Check (level-based tx limit)
 ├── AML Check (daily limit + structuring)
 ├── FX Service (optional conversion)
 │       ├── MockFxRateProvider (default)
 │       └── ExchangeRatesApiFxProvider (real)
 │
 ▼
RailRouter
 │
 ├── MockRail (local testing)
 ├── StripeRail       (Stripe sandbox)
 └── CbdcRail         (ISO 20022)
 │
      ├── CbdcBridgeResolver (MINT/REDEEM/TRANSFER/SWAP)
      │
      └── CbdcSandboxServer (internal mock network)
           ├── ECB Sandbox  (USDC proxy)
           ├── FED Sandbox  (USDT proxy)
           └── BIS mBridge  (cross-network SWAP)
      │
      ▼
      Ledger Service (DEBIT + CREDIT)
```

---

## Transaction Flow

```text
Register → Authenticate → Create Wallet → Deposit Funds
      ↓
Initiate Payment
      ↓
KYC Check (transaction limit by level)
      ↓
Balance Check
      ↓
FX Conversion (if receiveCurrency differs)
      ↓
AML Check (daily limit + structuring detection)
      ↓
RailRouter → MockRail | StripeRail | CbdcRail
      ↓
Record Ledger Entry (DEBIT sender / CREDIT receiver)
```

---

## Quick Start

Application runs on `http://localhost:8080/api`.

Swagger UI: `http://localhost:8080/api/swagger-ui.html`

---

## API Endpoints

### Auth

#### Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "Password123!"}'
```

Save `token` and `userId` from the response.

#### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "Password123!"}'
```

---

### Wallets

#### Create Wallet
```bash
curl -X POST "http://localhost:8080/api/v1/wallets?currency=USD" \
  -H "Authorization: Bearer <token>"
```

Supported currencies: `USD`, `EUR`, `GBP`, `AED`, `TRY`, `USDC`, `USDT`

#### Deposit Funds (sandbox only)
```bash
curl -X POST http://localhost:8080/api/v1/wallets/deposit \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000.00, "currency": "USD"}'
```

#### Get My Wallets
```bash
curl -X GET http://localhost:8080/api/v1/wallets \
  -H "Authorization: Bearer <token>"
```

---

### Compliance (KYC / AML)

#### Get My KYC Level
```bash
curl -X GET http://localhost:8080/api/v1/compliance/kyc/me \
  -H "Authorization: Bearer <token>"
```

#### Upgrade KYC Level
```bash
curl -X PUT http://localhost:8080/api/v1/compliance/kyc/{userId} \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"kycLevel": "BASIC", "notes": "verified manually"}'
```

#### Get My AML Flags
```bash
curl -X GET http://localhost:8080/api/v1/compliance/aml/me \
  -H "Authorization: Bearer <token>"
```

---

### Payments

All payment requests require `X-Idempotency-Key` header.

#### Payment — same currency (MockRail)
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: pay-001" \
  -d '{
    "receiverWalletAccountCode": "WALLET-{receiverUserId}-USD",
    "amount": 100.00,
    "currency": "USD",
    "railType": "MOCK",
    "description": "test payment"
  }'
```

#### Payment — with FX conversion
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: pay-002" \
  -d '{
    "receiverWalletAccountCode": "WALLET-{receiverUserId}-EUR",
    "amount": 100.00,
    "currency": "USD",
    "receiveCurrency": "EUR",
    "railType": "MOCK",
    "description": "fx payment"
  }'
```

Response includes `receiveAmount`, `receiveCurrency`, `fxRate`.

#### Payment — Stripe sandbox
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: pay-stripe-001" \
  -d '{
    "receiverWalletAccountCode": "WALLET-{receiverUserId}-USD",
    "amount": 50.00,
    "currency": "USD",
    "railType": "STRIPE",
    "description": "stripe payment"
  }'
```

Requires VPN and `STRIPE_API_KEY` environment variable.

#### CBDC Transfer (USDC → USDC)
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: cbdc-001" \
  -d '{
    "receiverWalletAccountCode": "WALLET-{receiverUserId}-USDC",
    "amount": 10.00,
    "currency": "USDC",
    "railType": "CBDC_SANDBOX",
    "description": "cbdc transfer"
  }'
```

Operation: `TRANSFER` → Network: `ECB_SANDBOX` → Status: `STLD` (immediate)

#### CBDC Mint (USD → USDC)
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: cbdc-mint-001" \
  -d '{
    "receiverWalletAccountCode": "WALLET-{receiverUserId}-USDC",
    "amount": 50.00,
    "currency": "USD",
    "receiveCurrency": "USDC",
    "railType": "CBDC_SANDBOX",
    "description": "mint usdc"
  }'
```

Operation: `MINT` → Network: `ECB_SANDBOX` → Status: `ACCP`

#### CBDC Swap (USDC → USDT cross-network)
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: cbdc-swap-001" \
  -d '{
    "receiverWalletAccountCode": "WALLET-{receiverUserId}-USDT",
    "amount": 10.00,
    "currency": "USDC",
    "receiveCurrency": "USDT",
    "railType": "CBDC_SANDBOX",
    "description": "cross-network swap"
  }'
```

Operation: `SWAP` → Network: `BIS_MBRIDGE` → Status: `ACCP`

#### Simulate Failed Payment
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: pay-fail-001" \
  -d '{
    "receiverWalletAccountCode": "WALLET-{receiverUserId}-USD",
    "amount": 50.00,
    "currency": "USD",
    "railType": "MOCK",
    "description": "fail this payment"
  }'
```

---

### Ledger

#### Get Ledger for a Payment
```bash
curl -X GET http://localhost:8080/api/v1/ledger/payments/{paymentId} \
  -H "Authorization: Bearer <token>"
```

#### Get Account History
```bash
curl -X GET "http://localhost:8080/api/v1/ledger/accounts/WALLET-{userId}-USD" \
  -H "Authorization: Bearer <token>"
```

---

### CBDC Sandbox (no auth required)

#### Query Available Networks
```bash
curl -X GET http://localhost:8080/api/cbdc-sandbox/networks
```

#### Query Transaction Status
```bash
curl -X GET http://localhost:8080/api/cbdc-sandbox/status/{txId}
```

---

## Automated Test

```bash
chmod +x test.sh
./test.sh
```

The script runs 20 steps covering: register, wallets, KYC upgrade, deposit, USD payment, FX payment, CBDC transfer, CBDC mint, balance check, ledger queries, failed payment, idempotency, and AML flags.

---

## FX Configuration

```yaml
app:
  fx:
    provider: mock                # default — no API key needed
    # provider: exchangeratesapi  # real rates
    api-key: your_key_here
```

Mock rates (USD base):

| Currency | Rate  |
|----------|-------|
| EUR      | 0.91  |
| GBP      | 0.79  |
| AED      | 3.67  |
| TRY      | 32.50 |
| USDT     | 1.00  |
| USDC     | 1.00  |

---

## KYC Levels

| Level      | Max Transaction | Daily Limit |
|------------|----------------|---------------|
| UNVERIFIED | 100 USD        | 500 USD     |
| BASIC      | 5,000 USD      | 20,000 USD  |
| FULL       | 1,000,000 USD  | 1,000,000 USD |

---

## AML Rules

| Rule | Description |
|-------------------|---------------------------------------------------|
| Daily Limit | Total daily payments cannot exceed KYC daily limit |
| Structuring | Round amounts ≥ 5,000 are flagged for review |

---

## CBDC Bridge Operations

| From    | To      | Operation | Network      | ISO 20022 Status |
|---------|---------|-----------|--------------|------------------|
| USD/EUR | USDC    | MINT      | ECB Sandbox  | ACCP             |
| USD/EUR | USDT    | MINT      | FED Sandbox  | ACCP             |
| USDC    | USD/EUR | REDEEM    | ECB Sandbox  | ACCP             |
| USDT    | USD/EUR | REDEEM    | FED Sandbox  | ACCP             |
| USDC    | USDC    | TRANSFER  | ECB Sandbox  | STLD (immediate) |
| USDT    | USDT    | TRANSFER  | FED Sandbox  | STLD (immediate) |
| USDC    | USDT    | SWAP      | BIS mBridge  | ACCP             |
| USDT    | USDC    | SWAP      | BIS mBridge  | ACCP             |

ISO 20022 status codes (pacs.002):

| Code | Meaning                        |
|------|--------------------------------|
| PDNG | Pending network confirmation   |
| ACCP | Accepted by network            |
| STLD | Settled — final, irreversible  |
| RJCT | Rejected (with reason code)    |

---

## Project Structure

```text
payment-bridge-core
├── config
├── security
├── common
│   └── enums (Currency, KycLevel, AmlStatus, CbdcNetwork, CbdcTxStatus, CbdcOperationType)
├── exception
├── user
├── payment
├── wallet
├── ledger
├── compliance
│   ├── entity (KycProfile, AmlFlag)
│   ├── service (KycService, AmlService)
│   └── controller (ComplianceController)
├── rails
│   ├── MockRail
│   ├── stripe (StripeRail)
│   └── cbdc
│       ├── CbdcRail
│       ├── CbdcBridgeResolver
│       ├── CbdcProperties
│       ├── dto (CbdcSettlementRequest, CbdcSettlementResponse)
│       └── sandbox (CbdcSandboxServer)
└── fx
    ├── provider (MockFxRateProvider, ExchangeRatesApiFxProvider)
    └── service (FxService)
```

---

## Implemented Features

| Feature          | Status | Notes                                      |
|------------------|--------|--------------------------------------------|
| Payment Core     | ✅     | lifecycle, idempotency, status tracking    |
| Auth & User      | ✅     | JWT, register, login                       |
| Wallet           | ✅     | multi-currency, balance from ledger        |
| FX Engine        | ✅     | mock + real provider, pluggable            |
| StripeRail       | ✅     | Stripe sandbox integration                 |
| KYC / AML        | ✅     | level-based limits, daily limit, flagging  |
| CBDC Layer       | ✅     | ISO 20022, 4 operations, 4 networks        |

---

## Design Principles

- build the core first, add complexity only when justified
- balance derived from ledger — never stored separately
- every payment rail behind a common interface
- every financial event recorded in the ledger
- compliance checks before every payment

---

## Disclaimer

This project is for educational and architectural exploration only.

Not designed for production use, regulatory compliance, or real-money handling.

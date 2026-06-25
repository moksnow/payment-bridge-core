# Payment Bridge

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/badge/Build-Active-orange.svg)]()
[![License](https://img.shields.io/badge/License-Internal-lightgrey.svg)]()

> A backend-first fintech infrastructure prototype for exploring payments, wallets, ledgers, and payment rails.

---

## Overview

Payment Bridge is a fintech infrastructure prototype that models how modern payment systems handle authentication, wallet management, payment execution, external payment rails, and financial record keeping.

The project is being developed incrementally to explore the architectural building blocks behind real-world financial platforms and future CBDC-oriented systems.

The goal is not to build a production bank or payment processor.

The goal is to understand the engineering concepts, system boundaries, and architectural decisions that power modern financial software.

---

## Why This Project Exists

Most developers integrate payment APIs without seeing the infrastructure that sits behind them.

This project explores:

- payment processing workflows
- wallet and balance management
- ledger-driven transaction recording
- payment rail abstraction
- FX conversion and multi-currency support
- KYC and AML compliance rules
- financial system architecture
- future CBDC concepts

---

## Tech Stack

| Category | Technology            |
|-----------|------------|
| Language | Java 21               |
| Framework | Spring Boot 3.5.x |
| Security | Spring Security + JWT |
| Database | H2                    |
| Migration | Flyway                |
| Documentation | OpenAPI / Swagger     |
| Testing | JUnit 5, Mockito      |

---

## Current Capabilities

The system currently supports:

- User registration and authentication
- JWT-based authorization
- Wallet creation and balance management
- Payment initiation and processing
- FX currency conversion (mock and real providers)
- Stripe sandbox integration
- KYC level enforcement per transaction
- AML daily limit and structuring detection
- Internal ledger recording

---

## Current Architecture

```text
User
 │
 ▼
Auth & JWT
 │
 ▼
Wallet
 │
 ▼
Payment Service
 │
 ├── KYC Check
 │       └── KycService (level-based limits)
 │
 ├── AML Check
 │       └── AmlService (daily limit + structuring)
 │
 ├── FX Service (optional conversion)
 │       ├── MockFxRateProvider (default)
 │       └── ExchangeRatesApiFxProvider (real)
 │
 ▼
RailRouter
 │
 ├── MockRail (local testing)
 │
 └── StripeRail (sandbox)
      │
      ▼
 Ledger Service
```

---

## Transaction Flow

```text
Register User
      ↓
Authenticate
      ↓
Create Wallet
      ↓
Deposit Funds
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
RailRouter → MockRail or StripeRail
      ↓
Record Ledger Entry (DEBIT sender / CREDIT receiver)
```

---

## Quick Start

Once the application is running on `http://localhost:8080`, use the following sequence to test the full payment flow.

Swagger UI is available at `http://localhost:8080/api/swagger-ui.html`.

### 1. Register

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'
```

Save the `token` and `userId` from the response.

### 2. Create Wallet

```bash
curl -X POST "http://localhost:8080/api/v1/wallets?currency=USD" \
  -H "Authorization: Bearer <token>"
```

### 3. Deposit Funds (sandbox)

```bash
curl -X POST http://localhost:8080/api/v1/wallets/deposit \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000.00, "currency": "USD"}'
```

### 4. Check KYC Level

```bash
curl -X GET http://localhost:8080/api/v1/compliance/kyc/me \
  -H "Authorization: Bearer <token>"
```

Default level is `UNVERIFIED` — max transaction `100 USD`, daily limit `500 USD`.

### 5. Upgrade KYC Level

```bash
curl -X PUT http://localhost:8080/api/v1/compliance/kyc/{userId} \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"kycLevel": "BASIC", "notes": "verified manually"}'
```

### 6. Initiate Payment (same currency)

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

### 7. Initiate Payment with FX conversion

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

### 8. Check Balance

```bash
curl -X GET http://localhost:8080/api/v1/wallets \
  -H "Authorization: Bearer <token>"
```

### 9. View Ledger

```bash
curl -X GET http://localhost:8080/api/v1/ledger/payments/{paymentId} \
  -H "Authorization: Bearer <token>"
```

### 10. View Account History

```bash
curl -X GET "http://localhost:8080/api/v1/ledger/accounts/WALLET-{userId}-USD" \
  -H "Authorization: Bearer <token>"
```

### 11. Simulate a Failed Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: pay-003" \
  -d '{
    "receiverWalletAccountCode": "WALLET-{receiverUserId}-USD",
    "amount": 50.00,
    "currency": "USD",
    "railType": "MOCK",
    "description": "fail this payment"
  }'
```

### 12. View AML Flags

```bash
curl -X GET http://localhost:8080/api/v1/compliance/aml/me \
  -H "Authorization: Bearer <token>"
```

### 13. CBDC Transfer (USDC → USDC)

```bash
# First create USDC wallets and deposit
curl -X POST "http://localhost:8080/api/v1/wallets?currency=USDC" \
  -H "Authorization: Bearer <token>"

curl -X POST http://localhost:8080/api/v1/wallets/deposit \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00, "currency": "USDC"}'

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

Response includes `externalRef` with CBDC network transaction ID (e.g. `CBDC-A1B2C3D4E5F6`).

### 14. CBDC Mint (USD → USDC)

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
    "description": "mint usdc from usd"
  }'
```

### 15. Query CBDC Networks

```bash
curl -X GET http://localhost:8080/api/cbdc-sandbox/networks
```

---

## Automated Test

A bash script is available to run the full flow end to end.

```bash
chmod +x test.sh
./test.sh
```

The script registers two users, creates wallets, deposits funds, upgrades KYC, runs a USD payment, runs a USD→EUR payment with FX conversion, checks balances, queries the ledger, simulates a failed payment, and verifies idempotency protection.

Each run uses unique idempotency keys so it can be executed multiple times cleanly.

---

## FX Configuration

Switch between mock and real FX provider in `application.yml`:

```yaml
app:
  fx:
    provider: mock                # default — fixed rates, no API key needed
    # provider: exchangeratesapi  # real rates from exchangeratesapi.io
    api-key: your_key_here
```

Mock rates (relative to USD):

| Currency | Rate  |
|----------|-------|
| USD      | 1.00  |
| EUR      | 0.91  |
| GBP      | 0.79  |
| AED      | 3.67  |
| TRY      | 32.50 |
| USDT     | 1.00  |
| USDC     | 1.00  |

---


## CBDC Bridge Operations

| From    | To      | Operation | Network      | Description                    |
|---------|---------|-----------|--------------|--------------------------------|
| USD/EUR | USDC    | MINT      | ECB Sandbox  | Fiat enters digital network    |
| USD/EUR | USDT    | MINT      | FED Sandbox  | Fiat enters digital network    |
| USDC    | USD/EUR | REDEEM    | ECB Sandbox  | Digital exits to fiat          |
| USDT    | USD/EUR | REDEEM    | FED Sandbox  | Digital exits to fiat          |
| USDC    | USDC    | TRANSFER  | ECB Sandbox  | Same-network transfer          |
| USDT    | USDT    | TRANSFER  | FED Sandbox  | Same-network transfer          |
| USDC    | USDT    | SWAP      | BIS mBridge  | Cross-network exchange         |
| USDT    | USDC    | SWAP      | BIS mBridge  | Cross-network exchange         |

CBDC Sandbox status codes follow ISO 20022 pacs.002:
- `PDNG` — Pending network confirmation
- `ACCP` — Accepted by network
- `STLD` — Settled (final, irreversible)
- `RJCT` — Rejected (with reason code)

## KYC Levels

| Level      | Max Transaction | Daily Limit |
|------------|----------------|-------------|
| UNVERIFIED | 100 USD        | 500 USD     |
| BASIC      | 5,000 USD      | 20,000 USD  |
| FULL       | 1,000,000 USD  | 1,000,000 USD |

---

## AML Rules

| Rule | Description |
|------|-------------|
| Daily Limit | Total daily payments cannot exceed KYC daily limit |
| Structuring | Round amounts ≥ 5,000 are flagged for review |

---

## Implemented Features

### Payment Core
- payment creation
- payment lifecycle (PENDING → PROCESSING → COMPLETED / FAILED)
- status tracking
- idempotency protection

### Auth & User
- user registration
- user authentication
- JWT-based security

### Wallet

- wallet creation per currency
- balance derived from ledger (bank-grade)
- sandbox deposit endpoint

### FX Engine

- pluggable provider interface
- MockFxRateProvider (default)
- ExchangeRatesApiFxProvider (real rates)

### Rails
- MockRail (local testing, fail simulation)
- StripeRail (Stripe sandbox)

### Compliance
- KYC level management (UNVERIFIED / BASIC / FULL)
- transaction limit enforcement per KYC level
- daily spending limit enforcement
- AML structuring detection
- compliance event recording


### CBDC Bridge
- ISO 20022 messaging (pacs.008 / pacs.002)
- four operation types: MINT, REDEEM, TRANSFER, SWAP
- multi-network routing: ECB Sandbox, FED Sandbox, BIS mBridge, Internal
- bridge logic: fiat↔CBDC and cross-network CBDC↔CBDC
- sandbox server simulating real CBDC network behavior
- operation-aware settlement lifecycle (PDNG → ACCP/STLD → RJCT)

### Ledger

- double-entry accounting (DEBIT + CREDIT)
- FX-aware credit entries
- deposit entries
- audit trail support

---

## Current Progress

```text
✅ Payment Core
✅ Auth & User
✅ StripeRail
✅ Wallet
✅ FX Engine
✅ KYC / AML
⏳ CBDC Layer
```

---

## Project Structure

```text
payment-bridge
├── config
├── security
├── common
│   └── enums
├── exception
├── user
├── payment
├── wallet
├── ledger
├── rails
│   └── stripe
├── fx
│   ├── dto
│   ├── provider
│   └── service
└── compliance
    ├── entity
    ├── dto
    ├── repository
    ├── service
    └── controller
```

---

## Architecture Complete

All planned milestones have been implemented.

The system now supports the full payment infrastructure stack:
from JWT auth and wallet management through FX conversion,
KYC/AML compliance, and CBDC settlement via ISO 20022 messaging.

---

## Design Principles

- build the core first
- keep boundaries clear
- abstract external providers
- record every financial event
- add complexity only when justified

---

## Disclaimer

This project is intended for educational and architectural exploration purposes only.

It is not designed for production use, regulatory compliance, or real-money handling.
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
- financial system architecture
- future compliance and CBDC concepts

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
- Internal ledger recording
- Rail abstraction for future payment providers

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
 ├── FX Service (optional conversion)
 │       │
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
 Stripe Sandbox
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
Balance Check
      ↓
FX Conversion (if receiveCurrency differs)
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

### 4. Initiate Payment (same currency)

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

### 5. Initiate Payment with FX conversion

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

Response includes `receiveAmount`, `receiveCurrency`, and `fxRate`.

Use `railType: STRIPE` to route through Stripe sandbox (requires VPN and API key).

### 6. Check Balance

```bash
curl -X GET http://localhost:8080/api/v1/wallets \
  -H "Authorization: Bearer <token>"
```

### 7. View Ledger

```bash
curl -X GET http://localhost:8080/api/v1/ledger/payments/{paymentId} \
  -H "Authorization: Bearer <token>"
```

### 8. View Account History

```bash
curl -X GET "http://localhost:8080/api/v1/ledger/accounts/WALLET-{userId}-USD" \
  -H "Authorization: Bearer <token>"
```

### 9. Simulate a Failed Payment

Add `"fail"` anywhere in the description to trigger a MockRail failure.

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

### 10. Test duplicate request protection

Send the same `X-Idempotency-Key` twice. The second request returns `409 CONFLICT`.

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: pay-001" \
  -d '{
    "receiverWalletAccountCode": "WALLET-{receiverUserId}-USD",
    "amount": 100.00,
    "currency": "USD",
    "railType": "MOCK"
  }'
```

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
- user-linked payment operations

### Wallet

- wallet creation per currency
- balance derived from ledger (bank-grade)
- sandbox deposit endpoint

### FX Engine

- pluggable provider interface
- MockFxRateProvider (default, no API key)
- ExchangeRatesApiFxProvider (real rates)
- cross-rate calculation via EUR base
- switchable via application.yml

### StripeRail
- Stripe Sandbox integration
- provider abstraction layer
- rail-based execution flow

### Ledger

- double-entry accounting (DEBIT + CREDIT)
- FX-aware credit entries
- deposit entries without payment reference
- audit trail support

---

## Current Progress

```text
✅ Payment Core
✅ Auth & User
✅ StripeRail
✅ Wallet
✅ FX Engine
⏳ KYC / AML
⏳ CBDC Layer
```

---

## Project Structure

```text
payment-bridge
├── config
├── security
├── common
├── exception
├── user
├── payment
├── wallet
├── ledger
├── rails
│   └── stripe
└── fx
    ├── dto
    ├── provider
    └── service
```

---

## Next Milestone

### KYC / AML

The next step is introducing basic compliance checks.

Planned goals:

- transaction limit enforcement
- user verification status
- suspicious pattern detection
- compliance event recording

---

## Design Principles

The project follows a gradual evolution strategy:

- build the core first
- keep boundaries clear
- abstract external providers
- record every financial event
- add complexity only when justified

This keeps the architecture understandable, testable, and easy to evolve.

---

## Disclaimer

This project is intended for educational and architectural exploration purposes only.

It is not designed for production use, regulatory compliance, or real-money handling.
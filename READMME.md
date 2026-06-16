# Payment Bridge

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/badge/Build-MVP-orange.svg)]()
[![License](https://img.shields.io/badge/License-Internal-lightgrey.svg)]()

> A backend-first fintech prototype for exploring how payment systems, internal ledgers, and payment rails are designed.

---

## Overview

Payment Bridge is a learning-focused financial infrastructure project built to understand the mechanics behind modern payment systems.

The project starts with a minimal payment core and evolves step by step toward a more complete architecture that includes authentication, real payment rails, wallet logic, FX handling, compliance, and future CBDC-oriented integrations.

The goal is not to build a production bank or a public payment product.  
The goal is to understand the engineering and architectural ideas behind them.

---

## Why This Project Exists

Most developers use payment APIs without seeing the structure behind them.

This project is meant to explore:

- how money movement is modeled in software
- how internal ledgers stay consistent
- how external payment rails are abstracted
- how a simple backend can grow into a financial infrastructure prototype

---

## Tech Stack

| Category | Technology            |
|----------|-----------------------|
| Language | Java 21               |
| Framework | Spring Boot 3.5.15     |
| Security | Spring Security + JWT |
| Database | H2                    |
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
Payment Service
 │
 ▼
MockRail
 │
 ▼
Ledger Service
```

---

## Implemented So Far

### Payment Core
- payment creation
- payment lifecycle handling
- status tracking
- core transaction flow

### Identity
- user registration
- user login
- JWT-based authentication
- payments linked to authenticated users

### Ledger
- basic ledger recording
- transaction traceability
- internal accounting trail

### Rail Abstraction
- `PaymentRail` interface
- `MockRail` implementation
- `RailRouter` for provider selection

---

## Project Structure

```text
payment-bridge
├── auth
├── user
├── payment
├── ledger
├── rails
├── common
├── config
└── exception
```

---

## Next Milestone

### StripeRail (Sandbox)

The next step is to replace the mock rail with Stripe Test Mode.

This will help simulate a real external payment flow while keeping the core system rail-agnostic.

What this milestone should introduce:

- external API integration
- mapping internal payment states to provider responses
- sandbox-only execution
- a clearer boundary between core business logic and payment provider behavior

---

## Roadmap

```text
Payment Core  ✅
Auth & User   ✅
StripeRail    ⏳
Wallet        ⏳
FX            ⏳
KYC / AML     ⏳
CBDC          ⏳
```

---

## Design Principle

The architecture is intentionally kept simple in the early stages.

Each new layer is added only when it creates real value for the system:

- core logic first
- identity second
- real rail integration next
- wallet and balance logic after that
- FX, compliance, and CBDC later

This keeps the project understandable, testable, and easy to evolve.

---

## Disclaimer

This project is for educational and architectural exploration only.

It is not intended for production use or real-money handling.
# Backend Implementation Guide

## Overview

This repository now contains the Spring Boot backend implementation for the Rydvrse MVP under `rydvrse-server/`. The backend is designed as a modular monolith with clear domain boundaries, PostgreSQL/PostGIS persistence, Flyway migrations, JWT-based authentication, auditable operational workflows, and API contracts aligned with the design documents.

## Implemented Modules

- `auth`
  - OTP login for customer and driver users
  - admin email/password login
  - refresh/logout flows
  - bootstrap admin provisioning
- `customer`
  - profile read/update
  - saved locations
  - home summary
- `master`
  - bootstrap config
  - serviceability checks
  - service zones and city metadata
- `pricing`
  - quote generation
  - pricing plans, payout plans, tax policies
  - fare snapshots
- `booking`
  - booking creation
  - list/detail
  - modification preview and apply
  - cancellation preview and cancel
  - post-trip ratings
- `driver`
  - onboarding
  - documents
  - availability
  - dashboard
  - earnings summary and ledger
  - upcoming jobs
- `trip`
  - assignment offers/detail
  - accept/decline
  - arrival
  - customer start confirmation
  - live location ingestion
  - tracking snapshot and SSE stream
  - share links
  - SOS
  - trip completion
- `finance`
  - payment orders
  - payment polling
  - invoices
  - refunds
  - payment webhook processing
- `support`
  - customer and driver ticket creation
  - admin ticket actions
  - incident actions
- `admin`
  - dashboard
  - rescue queue
  - reassignment
  - driver review
  - pricing/payout plan management
  - serviceability updates
  - audit log read
  - operations reporting

## Key Technical Decisions

- Framework: `Spring Boot 3.5`
- Language: `Java 21`
- Persistence: `Spring Data JPA + PostgreSQL/PostGIS`
- Migrations: `Flyway`
- API docs: `springdoc-openapi`
- Security: `Spring Security + JWT + BCrypt`
- Async/audit integration: outbox and audit tables
- Runtime cache/message infrastructure: Redis-ready configuration

## Local Run

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker Desktop or compatible Docker engine

### Start dependencies

```bash
cd rydvrse-server
docker compose up -d postgres redis
```

Local defaults are intentionally aligned as:

- PostgreSQL host port: `5433`
- PostgreSQL db: `rydvrse`
- PostgreSQL user: `rydvrse`
- PostgreSQL password: `rydvrse_secret`
- Redis host port: `6379`

### Start the backend

```bash
cd rydvrse-server
mvn -q -Dmaven.repo.local=/tmp/rydvrse-m2 spring-boot:run
```

### Health and docs

- Health: `http://localhost:8080/actuator/health`
- OpenAPI JSON: `http://localhost:8080/api/docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Test Commands

```bash
cd rydvrse-server
mvn -q -Dmaven.repo.local=/tmp/rydvrse-m2 test
```

Current automated coverage includes:

- booking rating service behavior
- finance payment flow behavior
- trip controller route behavior

## Environment Variables

Core environment variables:

- `RYDVRSE_DB_URL`
- `RYDVRSE_DB_USERNAME`
- `RYDVRSE_DB_PASSWORD`
- `RYDVRSE_REDIS_HOST`
- `RYDVRSE_REDIS_PORT`
- `RYDVRSE_JWT_SECRET`
- `RYDVRSE_ADMIN_EMAIL`
- `RYDVRSE_ADMIN_PASSWORD`
- `RYDVRSE_DEFAULT_OTP_CODE`
- `RYDVRSE_PAYMENTS_SANDBOX_AUTO_CAPTURE`
- `RYDVRSE_PAYMENTS_WEBHOOK_SECRET`

## Notes

- The backend assumes PostgreSQL with PostGIS because the schema uses geographic types and spatial lookups.
- Redis repository auto-discovery is disabled because Redis is used as infrastructure support, not as a Spring Data repository store.
- If a machine already has a local PostgreSQL instance on `5432`, the repo defaults now avoid that collision by using `5433` for the local Docker database.
- For production, all secrets must be provided through environment management and not rely on defaults.

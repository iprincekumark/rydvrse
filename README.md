# RYDVRSE — On-Demand Driver Marketplace Platform

> **Your Car. Our Driver. Your Destination.**

RYDVRSE is an enterprise-grade, technology-driven on-demand driver marketplace designed for the Indian mobility sector. Unlike ride-hailing platforms (Uber/Ola) where users book both a car and a driver, RYDVRSE allows users to book **only a professional driver** who operates the user's personal vehicle.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Module Map](#module-map)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [System Architecture](#system-architecture)
- [Module Details](#module-details)
- [System Workflows](#system-workflows)
- [Event-Driven Communication](#event-driven-communication)
- [Database Design](#database-design)
- [API Reference](#api-reference)
- [Production Readiness](#production-readiness)
- [Getting Started](#getting-started)

---

## Architecture Overview

RYDVRSE is built as a **Modular Monolith** — a single deployable Spring Boot application with internally separated, independently-bounded domain modules. Each module owns its domain models, business logic, and APIs, communicating through events and service interfaces.

```
┌──────────────────────────────────────────────────────────────────────┐
│                        RYDVRSE PLATFORM                              │
│                   Single Deployable Spring Boot App                   │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    API Gateway Layer                         │    │
│  │          Spring Security (JWT + RBAC) + Rate Limiting       │    │
│  └────────────────────────┬────────────────────────────────────┘    │
│                           │                                          │
│  ┌────────────────────────▼────────────────────────────────────┐    │
│  │                   REST Controllers                          │    │
│  │  Auth │ Customer │ Driver │ Trip │ Payment │ Admin │ Safety │    │
│  └────────────────────────┬────────────────────────────────────┘    │
│                           │                                          │
│  ┌────────────────────────▼────────────────────────────────────┐    │
│  │                Application Service Layer                     │    │
│  ├──────┬───────┬──────┬──────┬───────┬──────┬───────┬────────┤    │
│  │ Auth │ User  │Driver│Vehicl│ Trip  │Dispch│ Loctn │Pricing │    │
│  │      │       │      │      │       │      │       │        │    │
│  │Paymt │Wallet │Notif │Supprt│ Admin │ Ops  │Safety │Analytc │    │
│  └──────┴───────┴──────┴──────┴───────┴──────┴───────┴────────┘    │
│                           │                                          │
│  ┌────────────────────────▼────────────────────────────────────┐    │
│  │              Event Bus (Dual Channel)                        │    │
│  │    Spring ApplicationEvents (sync) + Kafka (async)          │    │
│  └────────────────────────┬────────────────────────────────────┘    │
│                           │                                          │
│  ┌────────────────────────▼────────────────────────────────────┐    │
│  │              Infrastructure Layer                            │    │
│  │     PostgreSQL │ Redis (GEO + Cache) │ Kafka │ WebSocket    │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

### Why Modular Monolith?

| Aspect | Benefit |
|--------|---------|
| **Development Speed** | Single codebase, no network hops, shared build |
| **Operational Simplicity** | One deployment, one database, one monitoring target |
| **Future Migration** | Clean module boundaries allow extraction to microservices |
| **Data Consistency** | Same-process transactions, no distributed coordination |
| **Team Scalability** | Each team owns a module with clear API contracts |

---

## Module Map

```
┌─────────────────────────────────────────────────────────────┐
│                        CORE MODULES                          │
├─────────┬─────────┬─────────┬─────────┬─────────┬──────────┤
│  Auth   │  User   │ Driver  │ Vehicle │  Trip   │ Dispatch │
│JWT+OTP  │Customer │Onboard  │Car Mgmt │Lifecycle│Matching  │
│RBAC     │Profile  │KYC+Docs │         │State M. │Algorithm │
├─────────┴─────────┴─────────┴─────────┴─────────┴──────────┤
│                     BUSINESS MODULES                         │
├─────────┬─────────┬─────────┬─────────┬─────────┬──────────┤
│Location │ Pricing │ Payment │ Wallet  │  Notif  │ Support  │
│GPS+Redis│Fare Eng.│Billing  │Balance  │Push/SMS │Tickets   │
│WebSocket│Surge    │Refunds  │Credits  │Email    │Chat      │
├─────────┴─────────┴─────────┴─────────┴─────────┴──────────┤
│                    PLATFORM MODULES                          │
├──────────────┬──────────────┬──────────────┬────────────────┤
│    Admin     │  Operations  │    Safety    │   Analytics    │
│  Dashboard   │  Doc Verify  │  SOS + BGV   │ Event Ingest  │
│  Management  │  Disputes    │  Incidents   │ Kafka Stream   │
└──────────────┴──────────────┴──────────────┴────────────────┘
```

---

## Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Language** | Java 21 | Modern Java with virtual threads support |
| **Framework** | Spring Boot 3.2 | Application framework |
| **Security** | Spring Security + JWT | Stateless authentication & RBAC |
| **ORM** | Spring Data JPA + Hibernate | Database access |
| **Database** | PostgreSQL 16 | Primary data store |
| **Cache** | Redis 7 | Driver locations (GEO), sessions, caching |
| **Messaging** | Apache Kafka | Async event streaming |
| **Real-time** | WebSocket (STOMP) | Live location tracking |
| **Build** | Maven (multi-module) | Build automation |
| **Docs** | SpringDoc OpenAPI | API documentation |

---

## Project Structure

```
rydvrse/
├── pom.xml                    # Parent POM (18 modules)
├── docker-compose.yml         # PostgreSQL, Redis, Kafka
│
├── rydvrse-shared/            # Shared Kernel
│   └── com.rydvrse.shared.
│       ├── domain/            # BaseEntity, GeoLocation
│       ├── dto/               # ApiResponse, PagedResponse
│       ├── enums/             # TripStatus, DriverStatus, PaymentStatus...
│       ├── event/             # DomainEvent, EventPublisher
│       └── exception/         # Exception hierarchy + GlobalExceptionHandler
│
├── rydvrse-auth/              # Authentication & Authorization
│   └── com.rydvrse.auth.
│       ├── api/               # AuthController
│       ├── config/            # SecurityConfig, JwtAuthenticationFilter
│       ├── domain/            # AuthUser, OtpRecord, RefreshToken
│       ├── dto/               # OtpRequest, AuthResponse...
│       ├── event/             # UserRegisteredEvent
│       ├── repository/        # AuthUserRepository, OtpRepository
│       └── service/           # AuthService, JwtTokenProvider
│
├── rydvrse-user/              # Customer Module
├── rydvrse-driver/            # Driver Module
├── rydvrse-vehicle/           # Vehicle Module
├── rydvrse-trip/              # Trip Module (Core Business)
├── rydvrse-dispatch/          # Dispatch/Matching Module
├── rydvrse-location/          # Location/GPS Module
├── rydvrse-pricing/           # Pricing/Fare Module
├── rydvrse-payment/           # Payment Module
├── rydvrse-wallet/            # Wallet Module
├── rydvrse-notification/      # Notification Module
├── rydvrse-support/           # Support Module
├── rydvrse-admin/             # Admin Module
├── rydvrse-operations/        # Operations Module
├── rydvrse-safety/            # Safety Module
├── rydvrse-analytics/         # Analytics Module
│
├── rydvrse-app/               # Main Application Entry Point
│   ├── RydvrseApplication.java
│   └── application.yml        # All configuration
│
└── docs/                      # Architecture Documentation
    ├── ARCHITECTURE.md
    ├── MODULE_DETAILS.md
    ├── DATABASE_DESIGN.md
    ├── EVENT_FLOWS.md
    ├── API_DESIGN.md
    ├── WORKFLOWS.md
    └── PRODUCTION_READINESS.md
```

---

## Module Details

### Auth Module
**Purpose**: Handles authentication and authorization for all user types.

| Aspect | Details |
|--------|---------|
| **Entities** | `AuthUser`, `OtpRecord`, `RefreshToken` |
| **Key Features** | OTP-based auth, JWT tokens, refresh token rotation, RBAC |
| **Events Published** | `UserRegisteredEvent` |
| **Dependencies** | None (foundation module) |
| **Communicates With** | User (profile creation), Driver (profile creation), Notification (welcome SMS) |

**Auth Flow**: Phone → OTP → Verify → JWT + Refresh Token → Profile Created

---

### User Module (Customer)
**Purpose**: Manages customer lifecycle and profile.

| Aspect | Details |
|--------|---------|
| **Entities** | `Customer`, `SavedLocation` |
| **Key Features** | Profile CRUD, saved locations, trip history access |
| **Events Consumed** | `UserRegisteredEvent` (auto-creates profile) |
| **Dependencies** | Auth (via events) |
| **Communicates With** | Trip, Payment, Notification, Support |

---

### Driver Module
**Purpose**: Manages driver lifecycle from onboarding to active operations.

| Aspect | Details |
|--------|---------|
| **Entities** | `Driver`, `DriverDocument` |
| **Key Features** | Onboarding, doc upload, KYC, availability toggle, performance tracking |
| **Events Published** | `DriverVerifiedEvent`, `DriverAvailabilityChangedEvent` |
| **Events Consumed** | `UserRegisteredEvent` (auto-creates profile) |
| **Driver States** | PENDING → DOCUMENT_SUBMITTED → UNDER_REVIEW → VERIFIED → ACTIVE |

---

### Vehicle Module
**Purpose**: Manages customer vehicle information.

| Aspect | Details |
|--------|---------|
| **Entities** | `Vehicle`, `VehicleDocument` |
| **Key Features** | Registration, document management, verification |
| **Communicates With** | Driver Module, Operations Module |

---

### Trip Module (Core Business)
**Purpose**: Owns the entire trip lifecycle — the central business entity.

| Aspect | Details |
|--------|---------|
| **Entities** | `Trip` |
| **Key Features** | State machine, OTP verification, fare tracking, ratings |
| **Events Published** | `TripRequestedEvent`, `TripStatusChangedEvent`, `TripCompletedEvent` |
| **Dependencies** | Dispatch, Pricing, Payment, Location, Notification, Safety |

**Trip Lifecycle State Machine**:
```
REQUESTED → DRIVER_MATCHING → DRIVER_ASSIGNED → DRIVER_ARRIVING
          → TRIP_STARTED → TRIP_COMPLETED

Any pre-start state → CANCELLED
```

---

### Dispatch Module
**Purpose**: Matches drivers with trip requests using a scoring algorithm.

| Aspect | Details |
|--------|---------|
| **Matching Algorithm** | Distance (40%) + Rating (30%) + Acceptance Rate (30%) |
| **Events Consumed** | `TripRequestedEvent` |
| **Dependencies** | Driver, Trip, Location |

---

### Location Module
**Purpose**: Real-time GPS tracking and spatial queries.

| Aspect | Details |
|--------|---------|
| **Technology** | Redis GEO + WebSocket (STOMP) |
| **Key Features** | Driver GPS ingestion, nearby driver search, live tracking |
| **Performance** | Redis GEORADIUS — O(N+log(M)) spatial queries |

---

### Pricing Module
**Purpose**: Fare calculation engine with city-specific rules.

| Aspect | Details |
|--------|---------|
| **Entities** | `PricingRule` |
| **Formula** | `fare = (baseFare + distanceKm × perKmRate + durationMin × perMinRate) × surge` |
| **Features** | City-specific rates, surge pricing, night surcharge (25%), minimum fare |

---

### Payment Module
**Purpose**: Full payment lifecycle management.

| Aspect | Details |
|--------|---------|
| **Entities** | `Payment` |
| **Events Consumed** | `TripCompletedEvent` (auto-creates payment) |
| **Events Published** | `PaymentCompletedEvent` |
| **Commission** | Platform takes 20%, Driver receives 80% |
| **Supports** | UPI, Credit/Debit Card, Wallet, Cash |

---

### Wallet Module
**Purpose**: User balance management for drivers and customers.

| Aspect | Details |
|--------|---------|
| **Entities** | `Wallet`, `WalletTransaction` |
| **Events Consumed** | `PaymentCompletedEvent` (auto-credits driver) |
| **Features** | Credits, debits, promo credits, full transaction audit trail |

---

### Notification Module
**Purpose**: Multi-channel notifications triggered by domain events.

| Aspect | Details |
|--------|---------|
| **Channels** | Push (FCM), SMS (Twilio), Email (SendGrid), In-App |
| **Triggered By** | Registration, trip status changes, payment, safety |

---

### Support Module
**Purpose**: Support ticket lifecycle management.

| Aspect | Details |
|--------|---------|
| **Entities** | `SupportTicket` |
| **Categories** | Payment, Trip, Safety, Driver, Other |
| **Priorities** | Low, Medium, High, Critical |

---

### Safety Module
**Purpose**: Trust and safety operations.

| Aspect | Details |
|--------|---------|
| **Entities** | `SafetyIncident` |
| **Features** | SOS emergency, incident reporting, safety monitoring |
| **Priority** | SOS incidents auto-escalated to CRITICAL |

---

### Admin Module
**Purpose**: Internal admin dashboard APIs (role-protected).

---

### Operations Module
**Purpose**: Internal operational workflows (driver verification, dispute resolution).

---

### Analytics Module
**Purpose**: Event ingestion pipeline for business intelligence.

---

## System Workflows

### 1. Customer Registration Flow
```
Customer           Auth Module        User Module       Notification
   │                   │                  │                  │
   ├──Send OTP────────►│                  │                  │
   │◄──OTP Sent────────┤                  │                  │
   │                   │                  │                  │
   ├──Verify OTP──────►│                  │                  │
   │                   ├──UserRegistered──►│                  │
   │                   │     Event        ├──Create Profile  │
   │                   │                  │                  │
   │                   ├──UserRegistered──────────────────────►│
   │                   │     Event                  Send Welcome SMS
   │◄──JWT + Refresh───┤                  │                  │
```

### 2. Driver Onboarding Flow
```
Driver            Auth          Driver Module      Operations       Admin
  │                │                │                  │              │
  ├──Register─────►│                │                  │              │
  │                ├──UserRegistered►│                  │              │
  │                │       Event    ├──Create Profile  │              │
  │◄──JWT──────────┤                │  (PENDING)       │              │
  │                                 │                  │              │
  ├──Upload Docs───────────────────►│                  │              │
  │                                 ├──Status:         │              │
  │                                 │  DOC_SUBMITTED   │              │
  │                                 │                  │              │
  │              Admin Reviews──────┼─────────────────►│              │
  │                                 │                  ├──approveDriver│
  │                                 │◄─────────────────┤              │
  │                                 ├──DriverVerified  │              │
  │                                 │  Status: ACTIVE  │              │
  │◄──Notification: You're Live!────┤                  │              │
```

### 3. Trip Booking Flow (End-to-End)
```
Customer      Trip Module    Dispatch      Driver      Location     Payment    Notification
   │              │             │            │            │            │            │
   ├──CreateTrip─►│             │            │            │            │            │
   │              ├──TripRequestedEvent─────►│            │            │            │
   │              │                          ├──Find      │            │            │
   │              │                          │  Nearby────►│            │            │
   │              │                          │  Drivers   │            │            │
   │              │                          │◄──Results──┤            │            │
   │              │                          ├──Score &   │            │            │
   │              │                          │  Select    │            │            │
   │              │◄──assignDriver───────────┤            │            │            │
   │              ├──TripStatusChanged───────┼────────────┼────────────┼───────────►│
   │              │  (DRIVER_ASSIGNED)       │            │            │   "Driver  │
   │              │                          │            │            │  Assigned!"│
   │              │                          │            │            │            │
   │◄──OTP+Driver Info──────────────────────►│            │            │            │
   │              │                          │            │            │            │
   │   Driver Drives to Customer             │            │            │            │
   │              │                          │◄─GPS──────►│            │            │
   │              │◄──driverArrived──────────┤            │    WebSocket│            │
   │              │                          │            │◄──Stream──►│            │
   │              │                          │            │            │            │
   ├──Share OTP──►│                          │            │            │            │
   │              ├──startTrip (OTP verify)  │            │            │            │
   │              │                          │            │            │            │
   │   ... Trip in Progress (GPS streaming) ...          │            │            │
   │              │                          │            │            │            │
   │              ├──completeTrip            │            │            │            │
   │              ├──TripCompletedEvent──────┼────────────┼───────────►│            │
   │              │                          │            │            ├──Process   │
   │              │                          │            │            │  Payment   │
   │              │                          │            │            ├──PaymentCompleted
   │              │                          │            │            │───────────►│
   │              │                          │            │            │  "₹X Paid" │
   │◄──Trip Summary+Receipt─────────────────┼────────────┼────────────┼────────────┤
```

### 4. Payment Processing Flow
```
TripCompleted     Payment Module      Gateway       Wallet Module    Notification
  Event              │                  │               │               │
   ├────────────────►│                  │               │               │
   │                 ├──Create Payment  │               │               │
   │                 │  (PENDING)       │               │               │
   │                 ├──Authorize──────►│               │               │
   │                 │◄──Authorized─────┤               │               │
   │                 ├──Capture────────►│               │               │
   │                 │◄──Captured───────┤               │               │
   │                 │                  │               │               │
   │                 │  Calculate:      │               │               │
   │                 │  Platform=20%    │               │               │
   │                 │  Driver=80%      │               │               │
   │                 │                  │               │               │
   │                 ├──PaymentCompleted────────────────►│               │
   │                 │  Event           │               ├──Credit       │
   │                 │                  │               │  Driver Wallet│
   │                 │                  │               │               │
   │                 ├──PaymentCompleted────────────────┼──────────────►│
   │                 │  Event           │               │   "₹X Paid"  │
```

### 5. Emergency Response Flow
```
Customer/Driver    Safety Module     Operations     Notification
      │                │                │               │
      ├──SOS──────────►│                │               │
      │   (lat, lng)   ├──Create        │               │
      │                │  Incident      │               │
      │                │  (CRITICAL)    │               │
      │                │                │               │
      │                ├──Alert─────────►               │
      │                │  Ops Team      │               │
      │                │                                │
      │                ├──Notify Emergency──────────────►│
      │                │  Contacts     │    Push+SMS    │
      │                │               │               │
      │   [In production: alert police via API]         │
```

---

## Event-Driven Communication

### Event Catalog

| Event | Producer | Consumers | Channel |
|-------|----------|-----------|---------|
| `UserRegisteredEvent` | Auth | User, Driver, Notification, Analytics | Sync + Async |
| `DriverVerifiedEvent` | Driver | Notification, Analytics | Sync + Async |
| `DriverAvailabilityChangedEvent` | Driver | Location | Sync |
| `TripRequestedEvent` | Trip | Dispatch, Notification, Analytics | Sync + Async |
| `TripStatusChangedEvent` | Trip | Notification, Analytics, Safety | Sync + Async |
| `TripCompletedEvent` | Trip | Payment, Driver, Analytics | Sync + Async |
| `PaymentCompletedEvent` | Payment | Wallet, Notification, Analytics | Sync + Async |

### Dual-Channel Architecture

```
┌──────────────┐     Spring Events (sync)     ┌──────────────┐
│   Producer   ├─────────────────────────────►│   Consumer   │
│   Module     │                               │   Module     │
│              ├─────────────────────────────►│              │
└──────────────┘     Kafka (async/durable)     └──────────────┘
```

- **Synchronous (Spring Events)**: For critical-path operations within the same transaction (e.g., Trip → Payment)
- **Asynchronous (Kafka)**: For analytics, notifications, and audit — durable and replayable

---

## Database Design

### Entity-Relationship Summary

| Table | Module | Key Relationships |
|-------|--------|-------------------|
| `auth_users` | Auth | → customers, drivers (via profile_id) |
| `otp_records` | Auth | → auth_users (via phone) |
| `refresh_tokens` | Auth | → auth_users |
| `customers` | User | → auth_users, → saved_locations |
| `saved_locations` | User | → customers |
| `drivers` | Driver | → auth_users, → driver_documents |
| `driver_documents` | Driver | → drivers |
| `vehicles` | Vehicle | → customers (owner_id), → vehicle_documents |
| `vehicle_documents` | Vehicle | → vehicles |
| `trips` | Trip | → customers, → drivers, → vehicles |
| `pricing_rules` | Pricing | Independent |
| `payments` | Payment | → trips, → customers, → drivers |
| `wallets` | Wallet | → customers/drivers (user_id) |
| `wallet_transactions` | Wallet | → wallets |
| `support_tickets` | Support | → customers/drivers (user_id), → trips |
| `safety_incidents` | Safety | → trips |

### Key Indexes

All entities use **UUID** primary keys for distributed-readiness. Key indexes include:
- `auth_users`: phone_number (unique), email, role
- `drivers`: status, is_available, operating_city
- `trips`: customer_id, driver_id, status, created_at
- `payments`: trip_id, transaction_id (unique), status

---

## API Reference

### Auth APIs (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/otp/send` | Send OTP to phone |
| POST | `/api/v1/auth/otp/verify` | Verify OTP & login |
| POST | `/api/v1/auth/token/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | Revoke all sessions |

### Customer APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/customers/me` | Get authenticated profile |
| GET | `/api/v1/customers/{id}` | Get customer by ID |
| PUT | `/api/v1/customers/{id}` | Update profile |

### Driver APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/drivers/me` | Get authenticated driver profile |
| PUT | `/api/v1/drivers/{id}/availability` | Toggle online/offline |
| POST | `/api/v1/drivers/{id}/documents` | Upload KYC document |

### Trip APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/trips` | Create trip request |
| GET | `/api/v1/trips/{id}` | Get trip details |
| POST | `/api/v1/trips/{id}/start` | Start trip (OTP) |
| POST | `/api/v1/trips/{id}/complete` | Complete trip |
| POST | `/api/v1/trips/{id}/cancel` | Cancel trip |
| POST | `/api/v1/trips/{id}/rate` | Rate driver |
| GET | `/api/v1/trips/customer/{id}` | Customer trip history |

### Location APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/locations/driver/{id}` | Update GPS |
| GET | `/api/v1/locations/driver/{id}` | Get driver location |
| GET | `/api/v1/locations/nearby` | Find nearby drivers |

### Admin APIs (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/dashboard/stats` | Dashboard statistics |
| POST | `/api/v1/admin/drivers/{id}/approve` | Approve driver |
| POST | `/api/v1/admin/drivers/{id}/reject` | Reject driver |

---

## Production Readiness

### Caching Strategy
- **Redis** for driver locations (TTL: 30 min)
- **Redis** for session data and rate limiting
- **Spring Cache** for pricing rules and static data

### Rate Limiting
- Auth endpoints: 5 req/min per IP
- Trip creation: 3 req/min per user
- Location updates: 60 req/min per driver

### Retry & Circuit Breaker
- Payment gateway: 3 retries with exponential backoff
- External SMS/Email: async with retry queue via Kafka
- Circuit breaker on external service calls (Resilience4j)

### Observability
- **Metrics**: Micrometer → Prometheus → Grafana
- **Tracing**: Spring Cloud Sleuth / Micrometer Tracing
- **Logging**: Structured JSON logs with traceId correlation
- **Health**: Spring Actuator health endpoints

### Monitoring Dashboards
- Trip throughput (requests/sec by city)
- Driver availability heatmap
- Payment success/failure rates
- SOS incident response time
- API latency percentiles (p50, p95, p99)

---

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose

### 1. Start Infrastructure
```bash
docker-compose up -d
```

### 2. Build the Project
```bash
mvn clean compile -DskipTests
```

### 3. Run the Application
```bash
cd rydvrse-app
mvn spring-boot:run
```

### 4. Access APIs
- Application: http://localhost:8080/api
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- Health: http://localhost:8080/api/actuator/health

---

## Business Model Support

The architecture is designed to support future expansion:

| Feature | How |
|---------|-----|
| **B2B Corporate Bookings** | Add `CorporateAccount` entity in User module, extend Trip with `booking_type` |
| **Subscription Plans** | New `Subscription` module, extend Pricing with plan-based discounts |
| **Fleet Partnerships** | Extend Driver module with `fleet_id`, add Fleet management APIs |
| **Microservice Migration** | Replace Spring Events with Kafka, extract modules as separate services |

---

## License

Proprietary — RYDVRSE Platform © 2026
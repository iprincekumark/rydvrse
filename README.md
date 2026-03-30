# RYDVRSE — On-Demand Driver Marketplace Platform

> **Your Car. Our Driver. Your Destination.**

RYDVRSE is an on-demand driver marketplace for India where users book professional drivers for their own vehicles. Unlike Uber/Ola, RYDVRSE connects car owners with verified drivers — no fleet, just your car and our driver.

---

## Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Modules](#modules)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Business Model](#business-model)

---

## Architecture

**Modular Monolith** — single deployable Spring Boot app with 10 internally-bounded domain modules, each owning its entities, business logic, and APIs.

```
┌──────────────────────────────────────────────────────────────────┐
│                       RYDVRSE PLATFORM                           │
│                 Single Deployable Spring Boot App                 │
├──────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │                   API Gateway Layer                       │    │
│  │       Spring Security (JWT + RBAC) + Rate Limiting       │    │
│  └─────────────────────────┬────────────────────────────────┘    │
│                             │                                      │
│  ┌─────────────────────────▼────────────────────────────────┐    │
│  │                  REST Controllers                         │    │
│  │  Auth │ Customer │ Driver │ Trip │ Payment │ Safety │ Admin│   │
│  └─────────────────────────┬────────────────────────────────┘    │
│                             │                                      │
│  ┌─────────────────────────▼────────────────────────────────┐    │
│  │                Application Services                       │    │
│  ├──────┬────────┬───────┬──────┬───────┬───────┬──────────┤    │
│  │ Auth │Customer│Driver │ Trip │Payment│Safety │  Admin   │    │
│  │      │        │       │      │       │       │          │    │
│  │      │        │       │      │Notif. │       │          │    │
│  └──────┴────────┴───────┴──────┴───────┴───────┴──────────┘    │
│                             │                                      │
│  ┌─────────────────────────▼────────────────────────────────┐    │
│  │               Event Bus (Spring Events)                   │    │
│  │          synchronous — Kafka-ready interfaces             │    │
│  └─────────────────────────┬────────────────────────────────┘    │
│                             │                                      │
│  ┌─────────────────────────▼────────────────────────────────┐    │
│  │               Infrastructure Layer                        │    │
│  │     PostgreSQL/PostGIS │ Redis │ Flyway │ WebSocket       │    │
│  └──────────────────────────────────────────────────────────┘    │
│                                                                    │
└──────────────────────────────────────────────────────────────────┘
```

### Why Modular Monolith?

| Aspect | Benefit |
|--------|---------|
| **Development Speed** | Single codebase, no network hops, shared build |
| **Operational Simplicity** | One deployment, one database, one monitoring target |
| **Future Migration** | Clean module boundaries → extract to microservices when needed |
| **Data Consistency** | Same-process transactions, no distributed coordination |

---

## Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Language** | Java 21 (runs on JDK 25) | Modern Java |
| **Framework** | Spring Boot 3.2.3 | Application framework |
| **Security** | Spring Security + JWT (jjwt 0.12.5) | Stateless auth + RBAC |
| **ORM** | Spring Data JPA + Hibernate Spatial | Database access + geospatial |
| **Database** | PostgreSQL 16 + PostGIS 3.4 | Primary data store + spatial queries |
| **Cache** | Redis 7 | OTP storage, rate limiting, idempotency, notification dedup |
| **Real-time** | WebSocket (STOMP) | Live location tracking |
| **Migrations** | Flyway | Schema versioning |
| **Build** | Maven (multi-module) | Build automation |
| **API Docs** | SpringDoc OpenAPI 2.3 | Swagger UI |
| **Annotations** | Lombok (edge) | Boilerplate reduction |

---

## Project Structure

```
rydvrse-server/
├── pom.xml                          # Parent POM (10 modules)
├── docker-compose.yml               # PostGIS + Redis
│
├── rydvrse-shared/                  # Shared Kernel
│   └── com.rydvrse.shared.
│       ├── domain/                  # BaseEntity (UUID PK, audit fields, version)
│       ├── dto/                     # ApiResponse, PagedResponse
│       ├── enums/                   # 27 domain enums
│       ├── event/                   # DomainEvent, EventPublisher (Spring → Kafka-ready)
│       ├── exception/               # 6 exceptions + GlobalExceptionHandler
│       ├── config/                  # SecurityConfig (§7.2 auth matrix)
│       ├── security/               # JwtAuthenticationFilter, UserPrincipal
│       ├── interceptor/            # RateLimitInterceptor (Redis sliding window)
│       ├── service/                # IdempotencyService (Redis, 24h TTL)
│       └── audit/                  # AuditLog, FailedEvent entities
│
├── rydvrse-auth/                    # Authentication (OTP + JWT)
│   └── entity/ repository/ service/ controller/ dto/
│
├── rydvrse-customer/                # Customer profiles + addresses
│   └── entity/ repository/ service/ controller/
│
├── rydvrse-driver/                  # Drivers + PostGIS locations
│   └── entity/ repository/ service/ controller/
│
├── rydvrse-trip/                    # Trip lifecycle + fare calc
│   └── entity/ repository/ service/ controller/
│
├── rydvrse-payment/                 # Payments + wallet
│   └── entity/ repository/ service/ controller/
│
├── rydvrse-safety/                  # SOS + incidents + sharing
│   └── entity/ repository/ service/ controller/
│
├── rydvrse-notification/            # Push/SMS + device tokens
│   └── entity/ repository/ service/ controller/
│
├── rydvrse-admin/                   # Dashboard + management
│   └── entity/ controller/
│
└── rydvrse-app/                     # Spring Boot assembly
    ├── RydvrseApplication.java
    ├── application.yml
    └── db/migration/V1__init.sql    # Flyway: 20 tables + PostGIS indexes
```

---

## Modules

### Auth Module — 4 endpoints
OTP-based authentication with Redis-backed OTP (5-min TTL, 3-attempt limit), JWT access tokens (15 min), refresh token rotation (30 days), and logout.

### Customer Module — 5 endpoints
Customer profile CRUD and saved address management (home, work, etc.).

### Driver Module — 6 endpoints
Driver profile, KYC document upload/verification, availability toggle, and **PostGIS GPS location** with `ST_DWithin` spatial queries for nearby driver search.

### Trip Module — 7 endpoints
Full trip lifecycle with **TripStateMachine** enforcing valid state transitions:
```
PENDING → SEARCHING_DRIVER → DRIVER_ASSIGNED → DRIVER_EN_ROUTE
→ DRIVER_ARRIVED → IN_PROGRESS → COMPLETED
↘ CANCELLED (from any pre-IN_PROGRESS state)
```
Fare estimation using Haversine distance, night surcharge detection (11pm–5am), configurable fare rules per vehicle type.

### Payment Module — 3 endpoints
Idempotent payment processing with Razorpay gateway stub, webhook endpoint, and wallet management.

### Safety Module — 6 endpoints
SOS alerts (trigger/resolve/active list), incident reporting with priority levels, and trip sharing via secure links.

### Notification Module — 1 endpoint + internal service
Device token registration (FCM), multi-channel notification service with Redis dedup (5-min TTL).

### Admin Module — 8 endpoints
Platform dashboard with real-time stats, customer/driver/trip/payment/safety/incident/fare-rule management.

---

## API Reference

### Auth APIs (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/otp/send` | Send OTP to phone |
| POST | `/api/v1/auth/otp/verify` | Verify OTP → JWT tokens |
| POST | `/api/v1/auth/refresh` | Refresh access token (rotation) |
| POST | `/api/v1/auth/logout` | Revoke all refresh tokens |

### Customer APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/customers/me` | Get authenticated profile |
| PUT | `/api/v1/customers/me` | Update profile |
| POST | `/api/v1/customers/me/addresses` | Add saved address |
| GET | `/api/v1/customers/me/addresses` | List saved addresses |
| DELETE | `/api/v1/customers/me/addresses/{id}` | Delete saved address |

### Driver APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/drivers/me` | Get driver profile |
| PUT | `/api/v1/drivers/me` | Update driver profile |
| POST | `/api/v1/drivers/me/documents` | Upload KYC document |
| PUT | `/api/v1/drivers/me/availability` | Toggle online/offline |
| PUT | `/api/v1/drivers/me/location` | Update GPS location |
| GET | `/api/v1/drivers/nearby` | Find nearby drivers (PostGIS) |

### Trip APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/trips/estimate` | Get fare estimate |
| POST | `/api/v1/trips/book` | Book a trip |
| GET | `/api/v1/trips/{id}` | Get trip details |
| PUT | `/api/v1/trips/{id}/cancel` | Cancel trip |
| PUT | `/api/v1/trips/{id}/start` | Start trip (driver) |
| PUT | `/api/v1/trips/{id}/complete` | Complete trip (driver) |
| POST | `/api/v1/trips/{id}/rate` | Rate the trip |

### Payment APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/payments/{id}` | Get payment status |
| POST | `/api/v1/payments/webhook` | Razorpay webhook |
| POST | `/api/v1/payments/wallet/topup` | Wallet operations |

### Safety APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/safety/sos` | Trigger SOS alert |
| PUT | `/api/v1/safety/sos/{id}/resolve` | Resolve SOS alert |
| GET | `/api/v1/safety/sos/active` | Get active SOS alerts |
| POST | `/api/v1/safety/incidents` | Report incident |
| GET | `/api/v1/safety/incidents/{id}` | Get incident details |
| GET | `/api/v1/safety/trips/{tripId}/share/{token}` | View shared trip (public) |

### Notification APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/notifications/device-token` | Register FCM token |

### Admin APIs (ADMIN role only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/dashboard/stats` | Platform metrics |
| GET | `/api/v1/admin/customers` | List customers |
| GET | `/api/v1/admin/drivers` | List drivers |
| GET | `/api/v1/admin/trips` | List trips |
| GET | `/api/v1/admin/payments` | Transaction list |
| GET | `/api/v1/admin/safety/sos` | SOS alerts |
| GET | `/api/v1/admin/safety/incidents` | Incident queue |
| GET | `/api/v1/admin/fare-rules` | Fare rules |

---

## Database Schema

**20 tables** managed via Flyway (`V1__init.sql`), with PostGIS extension for spatial queries.

| Table | Module | Key Features |
|-------|--------|-------------|
| `customer` | Customer | Unique phone + email, profile status |
| `customer_address` | Customer | Saved locations (home, work) |
| `driver` | Driver | Rating, trip count, acceptance rate |
| `driver_document` | Driver | KYC docs with verification status |
| `driver_location` | Driver | PostGIS Point + GIST spatial index |
| `driver_availability` | Driver | Online/offline + current trip |
| `trip` | Trip | Full lifecycle with composite indexes |
| `trip_location_log` | Trip | GPS breadcrumbs (PostGIS) |
| `trip_rating` | Trip | 1-5 stars + text review |
| `trip_dispatch_log` | Trip | Dispatch audit trail |
| `fare_rule` | Trip | Per vehicle type, effective dates |
| `payment` | Payment | Idempotency key (unique), gateway ref |
| `wallet` | Payment | Customer balance |
| `wallet_transaction` | Payment | Full transaction audit |
| `sos_alert` | Safety | Location + status tracking |
| `trip_share_link` | Safety | Secure token-based sharing |
| `incident` | Safety | Type + priority + status |
| `emergency_contact` | Safety | Per user emergency contacts |
| `notification_log` | Notification | Multi-channel delivery log |
| `device_token` | Notification | FCM tokens per platform |
| `admin_user` | Admin | Role-based admin accounts |
| `refresh_token` | Auth | JWT refresh token rotation |
| `audit_log` | Common | JSONB-based action audit |
| `failed_event` | Common | Dead-letter event store |

---

## Getting Started

### Prerequisites
- Java 21+ (tested on JDK 25)
- Maven 3.9+
- Docker & Docker Compose

### 1. Start Infrastructure
```bash
cd rydvrse-server
docker-compose up -d
# Starts PostGIS (port 5432) + Redis (port 6379)
```

### 2. Build the Project
```bash
mvn clean install -DskipTests
```

### 3. Run the Application
```bash
mvn spring-boot:run -pl rydvrse-app
```

### 4. Access APIs
| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **Health Check** | http://localhost:8080/actuator/health |
| **API Docs** | http://localhost:8080/v3/api-docs |

### Security

| Token | TTL | Purpose |
|-------|-----|---------|
| Access Token (JWT) | 15 minutes | API authentication |
| Refresh Token | 30 days | Token rotation |
| OTP | 5 minutes | Phone verification (3 attempts max) |

### Rate Limiting (Redis-backed)
- Auth endpoints: 5 req/min per IP
- Trip creation: 3 req/min per user
- Location updates: 60 req/min per driver

---

## Business Model

> **B2C Marketplace** — connecting verified drivers with car owners in India.

| Feature | Status |
|---------|--------|
| Customer booking & tracking | ✅ MVP |
| Driver onboarding & verification | ✅ MVP |
| Trip lifecycle with state machine | ✅ MVP |
| Payment processing (Razorpay) | ✅ MVP |
| SOS & safety features | ✅ MVP |
| Admin dashboard | ✅ MVP |
| B2B corporate bookings | 🔜 Planned |
| Subscription plans | 🔜 Planned |
| Fleet partnerships | 🔜 Planned |
| Microservice migration (Kafka) | 🔜 Planned |

---

## License

Proprietary — RYDVRSE Platform © 2026
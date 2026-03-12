# RYDVRSE System Architecture — Deep Dive

## 1. Architectural Pattern: Modular Monolith

RYDVRSE follows the **Modular Monolith** pattern — a middle ground between a traditional monolith and microservices.

### Why Not Microservices from Day One?
- **Operational overhead**: Running 16 services + gateways + service discovery for a startup is expensive
- **Network latency**: Sync calls between microservices add latency to every trip booking
- **Data consistency**: Trip lifecycle spans multiple bounded contexts — distributed transactions are complex
- **Team size**: Small team benefits from single codebase and shared deployment

### Module Isolation Rules
1. **Each module owns its data** — no cross-module direct table access
2. **Communication via events** — Spring ApplicationEvents for sync, Kafka for async
3. **Service interfaces** — modules expose services through interfaces, not internals
4. **Separate packages** — `com.rydvrse.{module}.*` isolation
5. **No circular dependencies** — dependency flows downward from shared kernel

### Future Microservice Extraction Path
```
Phase 1 (Current): Modular Monolith
  └── All modules in one Spring Boot app
  └── Spring Events for sync communication
  └── Kafka for async communication

Phase 2: Extract high-traffic modules
  └── Location → Separate service (high write throughput)
  └── Analytics → Separate service (Kafka consumer only)
  └── Notification → Separate service (external API calls)

Phase 3: Full microservices
  └── Each module → Own service + database
  └── Spring Events → replaced with Kafka/gRPC
  └── API Gateway → Spring Cloud Gateway
```

---

## 2. Data Ownership Model

Each module owns specific database tables. Cross-module data access happens ONLY through:
1. Events (eventually consistent)
2. Service method calls (same-transaction)
3. Shared value objects (GeoLocation, UUIDs)

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Auth Module  │  │ User Module  │  │ Driver Module│
│              │  │              │  │              │
│ auth_users   │  │ customers    │  │ drivers      │
│ otp_records  │  │ saved_locs   │  │ driver_docs  │
│ refresh_tkns │  │              │  │              │
└──────────────┘  └──────────────┘  └──────────────┘

┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Trip Module  │  │Payment Module│  │ Wallet Module│
│              │  │              │  │              │
│ trips        │  │ payments     │  │ wallets      │
│              │  │              │  │ wallet_txns  │
└──────────────┘  └──────────────┘  └──────────────┘

┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│Vehicle Module│  │Support Module│  │ Safety Module│
│              │  │              │  │              │
│ vehicles     │  │ support_tkts │  │ safety_incs  │
│ vehicle_docs │  │              │  │              │
└──────────────┘  └──────────────┘  └──────────────┘

┌──────────────┐
│Pricing Module│
│              │
│ pricing_rules│
└──────────────┘
```

---

## 3. Internal Communication Patterns

### Pattern 1: Synchronous In-Process Events
Used for operations that MUST happen in the same transaction.
```java
// Trip module publishes
eventPublisher.publish(new TripRequestedEvent(...));

// Dispatch module listens (same thread, same transaction)
@EventListener
public void onTripRequested(TripRequestedEvent event) { ... }
```

### Pattern 2: Asynchronous Kafka Events
Used for operations that can be eventually consistent.
```java
// Trip module publishes to Kafka
eventPublisher.publishAsync(new TripCompletedEvent(...));

// Analytics module consumes from Kafka topic
@KafkaListener(topics = "rydvrse.events.trip")
public void handleTripEvent(String event) { ... }
```

### Pattern 3: Direct Service Calls
Used when one module needs data from another within the same transaction.
```java
// Dispatch service directly calls DriverService
driverService.markOnTrip(driverId, true);
```

---

## 4. Transaction Boundaries

### Trip Creation Transaction
```
[Single PostgreSQL Transaction]
├── TripRepository.save(trip)
├── EventPublisher.publish(TripRequestedEvent)  ← sync, same txn
│   └── DispatchService.onTripRequested()       ← listener executes
│       ├── Find available drivers
│       ├── Score and match
│       ├── TripService.assignDriver()
│       └── DriverService.markOnTrip()
└── [Commit]
```

### Trip Completion Transaction
```
[Transaction 1 — Trip Module]
├── Trip.setStatus(COMPLETED)
├── TripRepository.save(trip)
├── EventPublisher.publish(TripCompletedEvent)  ← sync
│   └── PaymentService.onTripCompleted()
│       ├── Create Payment
│       └── Process Payment
│           ├── EventPublisher.publish(PaymentCompletedEvent)
│           │   └── WalletService.onPaymentCompleted()
│           │       └── Credit driver wallet
│           └── [All in same transaction]
└── [Commit]

[Async — Kafka]
├── Analytics.onTripCompleted()
├── Notification.onTripCompleted()
└── [Separate threads, eventually consistent]
```

---

## 5. Security Architecture

### Authentication Flow
```
1. Client sends phone number → OTP generated
2. Client sends OTP → verified against database
3. Server generates JWT (24h) + Refresh Token (7d)
4. JWT contains: userId, profileId, role
5. Every API request: Authorization: Bearer <jwt>
6. JwtAuthenticationFilter validates token
7. SecurityContext populated with userId + role
8. @PreAuthorize checks role-based access
```

### Role-Based Access Control (RBAC)
```
CUSTOMER  → /api/v1/customers/**, /api/v1/trips/**
DRIVER    → /api/v1/drivers/**, /api/v1/locations/**
ADMIN     → /api/v1/admin/**
OPERATIONS → /api/v1/operations/**
PUBLIC    → /api/v1/auth/**
```

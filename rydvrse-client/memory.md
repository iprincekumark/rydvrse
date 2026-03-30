# RYDVRSE — Complete Backend Memory Reference

> **For AI Agents building the React.js frontend.**
>
> This document contains every detail about the RYDVRSE backend — API endpoints, request/response schemas, domain models, enums, authentication, business logic, state machines, and WebSocket channels. Nothing is omitted.

---

## Table of Contents

1. [Platform Overview](#1-platform-overview)
2. [Architecture & Tech Stack](#2-architecture--tech-stack)
3. [Authentication & Security](#3-authentication--security)
4. [API Response Format](#4-api-response-format)
5. [API Endpoints — Complete Reference](#5-api-endpoints--complete-reference)
6. [Domain Models — Complete Schemas](#6-domain-models--complete-schemas)
7. [Enums — All Values](#7-enums--all-values)
8. [Business Logic & Lifecycles](#8-business-logic--lifecycles)
9. [WebSocket (Real-time)](#9-websocket-real-time)
10. [Pagination](#10-pagination)
11. [Error Handling](#11-error-handling)
12. [Frontend Pages & Features Map](#12-frontend-pages--features-map)
13. [Backend Connection Details](#13-backend-connection-details)

---

## 1. Platform Overview

**RYDVRSE** is an On-Demand Driver Marketplace for India.

**Core concept:** Users book ONLY a professional driver who operates the USER'S personal vehicle. Unlike Uber/Ola, the user provides the car — RYDVRSE provides the driver.

**Tagline:** "Your Car. Our Driver. Your Destination."

**User types:**
- **Customer** — books a driver, owns the vehicle
- **Driver** — professional driver operating the customer's car
- **Admin / Super Admin** — platform management, driver approvals
- **Operations** — day-to-day operational tasks

---

## 2. Architecture & Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.2 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Events | Kafka (Spring Events locally) |
| Auth | JWT (HS256) + OTP-based |
| API Docs | OpenAPI 3 / Swagger UI |
| Real-time | WebSocket (STOMP over SockJS) |
| Architecture | Modular Monolith (19 Maven modules) |

**Base URL:** `http://localhost:8080/api`

**All API paths start with:** `/api/v1/...`

---

## 3. Authentication & Security

### 3.1 Authentication Flow

The app uses **OTP-based phone authentication** (no passwords for customers/drivers).

```
Step 1: POST /v1/auth/otp/send     → { phoneNumber: "+919876543210" }
Step 2: POST /v1/auth/otp/verify   → { phoneNumber, otpCode, role }
Step 3: Receive JWT tokens          → { accessToken, refreshToken, userId, profileId, role, isNewUser }
Step 4: Use accessToken in header   → Authorization: Bearer <accessToken>
Step 5: Refresh when expired        → POST /v1/auth/token/refresh { refreshToken }
```

### 3.2 Phone Number Format

Indian phone numbers only: `+91` followed by 10 digits starting with 6-9.

```
Pattern: ^\\+91[6-9]\\d{9}$
Example: +919876543210
```

### 3.3 JWT Token Usage

All authenticated requests must include:

```
Authorization: Bearer <accessToken>
```

The JWT payload contains: `userId` (UUID), `role` (UserRole enum).

### 3.4 Public Endpoints (no token needed)

```
/v1/auth/**              — All auth endpoints
/actuator/health         — Health check
/v3/api-docs/**          — API documentation
/swagger-ui/**           — Swagger UI
OPTIONS /**              — CORS preflight
```

### 3.5 Role-Based Access

| Endpoint Pattern | Required Role |
|-----------------|---------------|
| `/v1/auth/**` | Public |
| `/v1/admin/**` | ADMIN or SUPER_ADMIN |
| `/v1/operations/**` | ADMIN or OPERATIONS |
| Everything else | Any authenticated user |

### 3.6 New User Detection

When `isNewUser: true` in the auth response, the frontend should redirect to the profile completion screen. The user's Customer or Driver profile has been auto-created with just the phone number — all other fields need to be filled.

---

## 4. API Response Format

### 4.1 Success Response

Every successful API response follows this structure:

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "timestamp": "2026-03-13T22:00:52.854673Z"
}
```

### 4.2 Error Response

```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Trip not found",
    "details": { ... }
  },
  "timestamp": "2026-03-13T22:00:52.854673Z"
}
```

### 4.3 Paginated Response

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  },
  "timestamp": "..."
}
```

> **Note:** `null` fields are omitted from JSON responses (Jackson `NON_NULL`).

---

## 5. API Endpoints — Complete Reference

### 5.1 Auth Controller — `/v1/auth`

#### `POST /v1/auth/otp/send` — Send OTP

**Auth:** Public

**Request:**
```json
{
  "phoneNumber": "+919876543210"
}
```
Validation: `phoneNumber` is required, must match `^\\+91[6-9]\\d{9}$`

**Response:**
```json
{
  "success": true,
  "message": "OTP sent",
  "data": {
    "phoneNumber": "+919876543210",
    "message": "OTP sent successfully",
    "expiresInSeconds": 300
  }
}
```

---

#### `POST /v1/auth/otp/verify` — Verify OTP & Login/Register

**Auth:** Public

**Request:**
```json
{
  "phoneNumber": "+919876543210",
  "otpCode": "1234",
  "role": "CUSTOMER"
}
```
- `role` determines profile type: `CUSTOMER` or `DRIVER`
- If user doesn't exist, auto-registers and creates profile
- `otpCode` is required, `role` is optional (defaults based on context)

**Response:**
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "a7b2c3d4-e5f6-...",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "profileId": "660e8400-e29b-41d4-a716-446655440001",
    "role": "CUSTOMER",
    "isNewUser": true
  }
}
```

---

#### `POST /v1/auth/token/refresh` — Refresh Access Token

**Auth:** Public

**Request:**
```json
{
  "refreshToken": "a7b2c3d4-e5f6-..."
}
```

**Response:** Same as verify (new accessToken + refreshToken pair)

---

#### `POST /v1/auth/logout` — Logout (revoke sessions)

**Auth:** Bearer token required

**Request:** Empty body

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

---

### 5.2 Customer Controller — `/v1/customers`

#### `GET /v1/customers/me` — Get My Profile

**Auth:** Bearer token (Customer)

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "phoneNumber": "+919876543210",
    "firstName": "Prince",
    "lastName": "Kumar",
    "email": "prince@example.com",
    "profileImageUrl": "https://...",
    "totalTrips": 15,
    "averageRating": 4.8
  }
}
```

---

#### `GET /v1/customers/{id}` — Get Customer Profile by ID

**Auth:** Bearer token

---

#### `PUT /v1/customers/{id}` — Update Customer Profile

**Auth:** Bearer token

**Request:**
```json
{
  "firstName": "Prince",
  "lastName": "Kumar",
  "email": "prince@example.com",
  "profileImageUrl": "https://...",
  "dateOfBirth": "1995-06-15",
  "gender": "MALE"
}
```
All fields are optional — only provided fields get updated.

---

### 5.3 Driver Controller — `/v1/drivers`

#### `GET /v1/drivers/me` — Get My Driver Profile

**Auth:** Bearer token (Driver)

**Response:** Returns full `Driver` object (see Domain Models section)

---

#### `GET /v1/drivers/{id}` — Get Driver Profile by ID

**Auth:** Bearer token

---

#### `PUT /v1/drivers/{id}/availability` — Toggle Availability

**Auth:** Bearer token (Driver)

**Request:**
```json
{
  "available": true
}
```

---

#### `POST /v1/drivers/{id}/documents` — Upload Document

**Auth:** Bearer token (Driver)

**Request:**
```json
{
  "documentType": "DRIVING_LICENSE",
  "documentUrl": "https://s3.../license.pdf",
  "documentNumber": "DL-1234567890"
}
```

Document types: `DRIVING_LICENSE`, `AADHAAR`, `PAN`, `PHOTO`, `ADDRESS_PROOF`

---

### 5.4 Trip Controller — `/v1/trips`

#### `POST /v1/trips` — Create Trip

**Auth:** Bearer token (Customer)

**Request:**
```json
{
  "customerId": "uuid",
  "vehicleId": "uuid (optional)",
  "pickupLat": 28.6139,
  "pickupLng": 77.2090,
  "pickupAddress": "Connaught Place, New Delhi",
  "pickupCity": "Delhi",
  "dropLat": 28.5355,
  "dropLng": 77.3910,
  "dropAddress": "Sector 62, Noida",
  "dropCity": "Noida",
  "estimatedFare": 350.0,
  "surgeMultiplier": 1.2
}
```
Required: `customerId`, `pickupLat`, `pickupLng`, `dropLat`, `dropLng`

**Response:** Returns full `Trip` object with `status: "REQUESTED"` and auto-generated `tripNumber` (e.g., `RYD-20260313-ABCD`) and `startOtp` (4-digit code for trip start verification).

---

#### `GET /v1/trips/{id}` — Get Trip Detail

**Auth:** Bearer token

---

#### `POST /v1/trips/{id}/assign-driver` — Assign Driver to Trip

**Auth:** Bearer token (System/Admin)

**Request:**
```json
{
  "driverId": "uuid"
}
```

---

#### `POST /v1/trips/{id}/driver-arrived` — Driver Arrived at Pickup

**Auth:** Bearer token (Driver)

**Request:** Empty body

---

#### `POST /v1/trips/{id}/start` — Start Trip (requires OTP verification)

**Auth:** Bearer token (Driver)

**Request:**
```json
{
  "otp": "1234"
}
```
The driver must enter the 4-digit OTP shown on the customer's screen to start the trip. This verifies the driver is at the correct customer.

---

#### `POST /v1/trips/{id}/complete` — Complete Trip

**Auth:** Bearer token (Driver)

**Request:**
```json
{
  "actualDistanceKm": 25.5,
  "actualDurationMin": 45
}
```

---

#### `POST /v1/trips/{id}/cancel` — Cancel Trip

**Auth:** Bearer token (Customer or Driver)

**Request:**
```json
{
  "reason": "Driver taking too long",
  "cancelledBy": "CUSTOMER"
}
```
`cancelledBy` values: `CUSTOMER`, `DRIVER`, `SYSTEM`

---

#### `POST /v1/trips/{id}/rate` — Rate Driver After Trip

**Auth:** Bearer token (Customer)

**Request:**
```json
{
  "rating": 4.5,
  "feedback": "Great driving, very professional"
}
```

---

#### `GET /v1/trips/customer/{customerId}?page=0&size=20` — Customer Trip History

**Auth:** Bearer token

**Response:** Paginated response (see PagedResponse format)

---

### 5.5 Location Controller — `/v1/locations`

#### `POST /v1/locations/driver/{driverId}` — Update Driver GPS Location

**Auth:** Bearer token (Driver)

**Request:**
```json
{
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

---

#### `GET /v1/locations/driver/{driverId}` — Get Driver Current Location

**Auth:** Bearer token

**Response:**
```json
{
  "success": true,
  "data": {
    "latitude": 28.6139,
    "longitude": 77.2090
  }
}
```

---

#### `POST /v1/locations/driver/{driverId}/trip/{tripId}` — Update Location During Trip (streams via WebSocket)

**Auth:** Bearer token (Driver)

**Request:**
```json
{
  "latitude": 28.6139,
  "longitude": 77.2090
}
```
This endpoint also pushes the location to WebSocket topic `/topic/trip/{tripId}/location`.

---

#### `GET /v1/locations/nearby?lat=28.61&lng=77.20&radiusKm=5.0` — Find Nearby Drivers

**Auth:** Bearer token

**Response:**
```json
{
  "success": true,
  "data": ["driverId1", "driverId2", "driverId3"]
}
```

---

### 5.6 Admin Controller — `/v1/admin`

> **All admin endpoints require role ADMIN or SUPER_ADMIN**

#### `GET /v1/admin/dashboard/stats` — Dashboard Statistics

**Response:**
```json
{
  "success": true,
  "data": {
    "totalDrivers": 150,
    "totalTrips": 5000
  }
}
```

---

#### `POST /v1/admin/drivers/{id}/approve` — Approve Driver

**Request:** Empty body

---

#### `POST /v1/admin/drivers/{id}/reject` — Reject Driver

**Request:**
```json
{
  "reason": "Incomplete documents"
}
```

---

#### `POST /v1/admin/disputes/{tripId}/resolve` — Resolve Trip Dispute

**Request:**
```json
{
  "resolution": "Refund issued to customer"
}
```

---

## 6. Domain Models — Complete Schemas

### 6.1 BaseEntity (inherited by all entities)

| Field | Type | Notes |
|-------|------|-------|
| `id` | UUID | Auto-generated primary key |
| `createdAt` | Instant | Auto-set on creation |
| `updatedAt` | Instant | Auto-set on update |
| `version` | Long | Optimistic locking (@Version) |

---

### 6.2 AuthUser

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `phoneNumber` | String(15) | Unique, not null |
| `email` | String(255) | Optional |
| `passwordHash` | String | Optional |
| `role` | UserRole enum | Not null |
| `profileId` | UUID | Links to Customer/Driver profile |
| `isActive` | Boolean | Default: true |
| `isPhoneVerified` | Boolean | Default: false |
| `isEmailVerified` | Boolean | Default: false |
| `lastLoginAt` | Instant | Nullable |
| `failedLoginAttempts` | Integer | Default: 0 |
| `lockedUntil` | Instant | Nullable |

---

### 6.3 Customer

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `authUserId` | UUID | Unique, not null — links to AuthUser |
| `phoneNumber` | String(15) | Unique, not null |
| `firstName` | String(100) | |
| `lastName` | String(100) | |
| `email` | String(255) | |
| `profileImageUrl` | String | |
| `dateOfBirth` | LocalDate | |
| `gender` | String(20) | |
| `preferredLanguage` | String(10) | Default: "en" |
| `totalTrips` | Integer | Default: 0 |
| `averageRating` | Double | Default: 5.0 |
| `isActive` | Boolean | Default: true |
| `savedLocations` | List\<SavedLocation\> | One-to-many |

---

### 6.4 SavedLocation

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `customer` | Customer | Many-to-one FK |
| `label` | String(50) | Not null, e.g., "Home", "Office", "Gym" |
| `location` | GeoLocation | Embedded (lat, lng, address, city, pincode) |
| `isDefault` | Boolean | Default: false |

---

### 6.5 Driver

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `authUserId` | UUID | Unique, not null |
| `phoneNumber` | String(15) | Unique, not null |
| `firstName` | String(100) | |
| `lastName` | String(100) | |
| `email` | String(255) | |
| `profileImageUrl` | String | |
| `dateOfBirth` | LocalDate | |
| `licenseNumber` | String(50) | |
| `licenseExpiry` | LocalDate | |
| `status` | DriverStatus enum | Default: PENDING_VERIFICATION |
| `isAvailable` | Boolean | Default: false |
| `isOnTrip` | Boolean | Default: false |
| `currentLocation` | GeoLocation | Embedded |
| `totalTrips` | Integer | Default: 0 |
| `averageRating` | Double | Default: 5.0 |
| `totalRatings` | Integer | Default: 0 |
| `acceptanceRate` | Double | Default: 100.0 (%) |
| `cancellationRate` | Double | Default: 0.0 (%) |
| `totalEarnings` | Double | Default: 0.0 (INR) |
| `documents` | List\<DriverDocument\> | One-to-many |
| `operatingCity` | String(100) | |

---

### 6.6 DriverDocument

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `driver` | Driver | Many-to-one FK |
| `documentType` | String(50) | Not null. Values: `DRIVING_LICENSE`, `AADHAAR`, `PAN`, `PHOTO`, `ADDRESS_PROOF` |
| `documentUrl` | String | Not null — URL to uploaded file |
| `documentNumber` | String(100) | e.g., license number |
| `verificationStatus` | VerificationStatus enum | Default: PENDING |
| `rejectionReason` | String | Nullable |
| `verifiedBy` | String | Nullable |

---

### 6.7 Vehicle

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `ownerId` | UUID | Customer who owns this vehicle |
| `registrationNumber` | String(20) | Unique, not null (e.g., "DL01AB1234") |
| `make` | String(50) | e.g., Maruti, Hyundai, Tata |
| `model` | String(50) | e.g., Swift, Creta, Nexon |
| `year` | Integer | e.g., 2023 |
| `color` | String(30) | |
| `fuelType` | String(20) | Values: `PETROL`, `DIESEL`, `CNG`, `ELECTRIC` |
| `transmission` | String(20) | Values: `MANUAL`, `AUTOMATIC` |
| `vehicleType` | String(30) | Values: `SEDAN`, `SUV`, `HATCHBACK` |
| `verificationStatus` | VerificationStatus enum | Default: PENDING |
| `documents` | List\<VehicleDocument\> | One-to-many |

---

### 6.8 Trip

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `tripNumber` | String(20) | Unique, auto-generated: `RYD-YYYYMMDD-XXXX` |
| `customerId` | UUID | Not null |
| `driverId` | UUID | Nullable (assigned later) |
| `vehicleId` | UUID | Nullable |
| `status` | TripStatus enum | Default: REQUESTED |
| `pickupLocation` | GeoLocation | Embedded (pickup_lat, pickup_lng, pickup_address, pickup_city, pickup_pincode) |
| `dropLocation` | GeoLocation | Embedded (drop_lat, drop_lng, drop_address, drop_city, drop_pincode) |
| `estimatedDistanceKm` | Double | |
| `actualDistanceKm` | Double | Set on completion |
| `estimatedDurationMin` | Integer | |
| `actualDurationMin` | Integer | Set on completion |
| `estimatedFare` | Double | INR |
| `finalFare` | Double | INR, calculated on completion |
| `surgeMultiplier` | Double | Default: 1.0 |
| `driverAssignedAt` | Instant | |
| `driverArrivedAt` | Instant | |
| `tripStartedAt` | Instant | |
| `tripCompletedAt` | Instant | |
| `cancelledAt` | Instant | |
| `cancellationReason` | String | |
| `cancelledBy` | String | Values: `CUSTOMER`, `DRIVER`, `SYSTEM` |
| `customerRating` | Double | 1.0–5.0, customer rates driver |
| `driverRating` | Double | 1.0–5.0, driver rates customer |
| `customerFeedback` | String | |
| `driverFeedback` | String | |
| `startOtp` | String(4) | 4-digit OTP to verify trip start |

---

### 6.9 Payment

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `tripId` | UUID | Not null |
| `customerId` | UUID | Not null |
| `driverId` | UUID | Not null |
| `transactionId` | String(50) | Unique, auto-generated: `PAY-XXXXXXXXXXXX` |
| `amount` | Double | Not null, INR |
| `platformFee` | Double | Default: 0.0 |
| `driverPayout` | Double | amount - platformFee |
| `currency` | String(3) | Default: "INR" |
| `paymentMethod` | PaymentMethod enum | Not null |
| `status` | PaymentStatus enum | Default: PENDING |
| `gatewayReference` | String | External payment gateway reference (Razorpay) |
| `paidAt` | Instant | |
| `failureReason` | String | |
| `refundAmount` | Double | |
| `refundedAt` | Instant | |

---

### 6.10 Wallet

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `userId` | UUID | Unique, not null |
| `userType` | String(20) | Values: `CUSTOMER`, `DRIVER` |
| `balance` | Double | Default: 0.0, INR |
| `currency` | String(3) | Default: "INR" |
| `isActive` | Boolean | Default: true |

---

### 6.11 WalletTransaction

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `walletId` | UUID | Not null |
| `amount` | Double | Not null |
| `transactionType` | String(20) | Values: `CREDIT`, `DEBIT`, `PROMO_CREDIT`, `REFUND` |
| `description` | String | |
| `referenceId` | UUID | Payment ID, Promo ID, etc. |
| `balanceAfter` | Double | Not null |

---

### 6.12 SupportTicket

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `ticketNumber` | String(20) | Unique, auto-generated: `TKT-XXXXXXXX` |
| `userId` | UUID | Not null |
| `userType` | String(20) | Values: `CUSTOMER`, `DRIVER` |
| `tripId` | UUID | Optional — link to related trip |
| `category` | String(50) | Values: `PAYMENT`, `TRIP`, `SAFETY`, `DRIVER`, `OTHER` |
| `subject` | String | Not null |
| `description` | String (TEXT) | |
| `status` | String(20) | Default: `OPEN`. Values: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED` |
| `priority` | String(10) | Default: `MEDIUM`. Values: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `assignedTo` | String | |
| `resolutionNotes` | String (TEXT) | |

---

### 6.13 SafetyIncident

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `tripId` | UUID | Optional |
| `reportedBy` | UUID | Reporter's user ID |
| `reporterType` | String(20) | Values: `CUSTOMER`, `DRIVER`, `SYSTEM` |
| `incidentType` | String(50) | Not null. Values: `SOS`, `ACCIDENT`, `HARASSMENT`, `RECKLESS_DRIVING`, `OTHER` |
| `description` | String (TEXT) | |
| `location` | GeoLocation | Embedded |
| `status` | String(20) | Default: `REPORTED`. Values: `REPORTED`, `INVESTIGATING`, `RESOLVED`, `ESCALATED` |
| `priority` | String(10) | Default: `HIGH` |
| `resolutionNotes` | String (TEXT) | |

---

### 6.14 PricingRule

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | UUID | PK |
| `city` | String(100) | Not null, indexed |
| `vehicleType` | String(30) | Values: `SEDAN`, `SUV`, `HATCHBACK`, `ANY` |
| `baseFare` | Double | Default: 50.0 (INR) |
| `perKmRate` | Double | Default: 12.0 (INR) |
| `perMinuteRate` | Double | Default: 2.0 (INR) |
| `minimumFare` | Double | Default: 80.0 (INR) |
| `nightSurchargePct` | Double | Default: 25.0 (%) — applies 11PM–6AM |
| `isActive` | Boolean | Default: true |

**Fare formula:** `max(baseFare + (distance × perKmRate) + (duration × perMinuteRate), minimumFare) × surgeMultiplier`

---

### 6.15 GeoLocation (embedded value object)

Used in Trip (pickup/drop), Driver (current location), SafetyIncident, SavedLocation.

| Field | Type |
|-------|------|
| `latitude` | Double |
| `longitude` | Double |
| `address` | String |
| `city` | String |
| `pincode` | String |

---

## 7. Enums — All Values

### UserRole
```
CUSTOMER, DRIVER, ADMIN, SUPER_ADMIN, OPERATIONS
```

### TripStatus
```
REQUESTED → DRIVER_MATCHING → DRIVER_ASSIGNED → DRIVER_ARRIVING → TRIP_STARTED → TRIP_COMPLETED
                                                                                ↗
Any pre-start state → CANCELLED
```

### DriverStatus
```
PENDING_VERIFICATION → DOCUMENT_SUBMITTED → UNDER_REVIEW → VERIFIED → ACTIVE
                                                                     ↓
                                                              INACTIVE / SUSPENDED / BLOCKED
```

### PaymentStatus
```
PENDING → AUTHORIZED → CAPTURED → COMPLETED
                                  ↓
                           FAILED / REFUNDED / PARTIALLY_REFUNDED
```

### PaymentMethod
```
UPI, CREDIT_CARD, DEBIT_CARD, WALLET, CASH
```

### VerificationStatus
```
PENDING → SUBMITTED → UNDER_REVIEW → APPROVED / REJECTED / EXPIRED
```

### NotificationChannel
```
PUSH, SMS, EMAIL, IN_APP
```

---

## 8. Business Logic & Lifecycles

### 8.1 Trip Lifecycle (CORE)

```
Customer creates trip
        ↓
   [REQUESTED]         ← Customer sees "Looking for driver"
        ↓
[DRIVER_MATCHING]      ← System searches nearby available drivers
        ↓
[DRIVER_ASSIGNED]      ← Driver accepted, customer sees driver details
        ↓
[DRIVER_ARRIVING]      ← Driver en route, customer sees live location
        ↓
  Customer shows 4-digit OTP → Driver enters OTP
        ↓
  [TRIP_STARTED]       ← Meter starts, live tracking active
        ↓
  [TRIP_COMPLETED]     ← Driver marks complete, final fare calculated
        ↓
  Customer rates driver (1-5 stars + feedback)
        ↓
  Payment processed
```

**Cancellation:** Any state before `TRIP_STARTED` can be cancelled by customer, driver, or system. After trip starts, only system/admin can cancel (dispute resolution).

**OTP verification:** The customer's app shows a 4-digit `startOtp`. The driver must enter this OTP to start the trip. This prevents wrong pickups.

### 8.2 Driver Onboarding Lifecycle

```
Driver registers (phone verification)
        ↓
 [PENDING_VERIFICATION]   ← Profile created, no documents yet
        ↓
  Driver uploads documents (license, Aadhaar, PAN, photo)
        ↓
 [DOCUMENT_SUBMITTED]     ← Awaiting admin review
        ↓
 [UNDER_REVIEW]            ← Admin reviewing documents
        ↓
 [VERIFIED] → [ACTIVE]    ← Admin approves, driver can go online
    OR
 [REJECTED]                ← Admin rejects with reason
```

**Required documents:**
1. `DRIVING_LICENSE` — with license number and expiry date
2. `AADHAAR` — Aadhaar card
3. `PAN` — PAN card
4. `PHOTO` — Profile photo
5. `ADDRESS_PROOF` — Address verification

### 8.3 Payment Lifecycle

```
Trip completed
      ↓
  [PENDING]        ← Payment initiated
      ↓
  [AUTHORIZED]     ← Funds reserved (card/UPI)
      ↓
  [CAPTURED]       ← Funds deducted from customer
      ↓
  [COMPLETED]      ← Driver payout scheduled

  Payment failures → [FAILED] + failureReason
  Disputes/cancellations → [REFUNDED] or [PARTIALLY_REFUNDED]
```

**Platform fee:** Deducted from total amount → `driverPayout = amount - platformFee`

### 8.4 Driver Availability

- Driver toggles `isAvailable` on/off (going online/offline)
- When assigned a trip, `isOnTrip` becomes true, `isAvailable` becomes false
- After trip completion, `isOnTrip` becomes false (driver can toggle available again)
- Only `ACTIVE` + `isAvailable = true` + `isOnTrip = false` drivers appear in dispatch search

### 8.5 Fare Calculation

```
fare = baseFare + (distanceKm × perKmRate) + (durationMin × perMinuteRate)
finalFare = max(fare, minimumFare) × surgeMultiplier
```

- Night surcharge (11 PM – 6 AM): add `nightSurchargePct`% to base rate
- Surge pricing: multiplier > 1.0 during high demand periods
- Pricing rules are per-city and per-vehicle-type

---

## 9. WebSocket (Real-time)

### Connection

Protocol: STOMP over SockJS

```
Endpoint: /api/ws
```

### Topics

| Topic | Data | Purpose |
|-------|------|---------|
| `/topic/trip/{tripId}/location` | `{ latitude, longitude }` | Live driver location during active trip |

### Usage in Frontend

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/api/ws'),
  onConnect: () => {
    client.subscribe(`/topic/trip/${tripId}/location`, (message) => {
      const location = JSON.parse(message.body);
      // { latitude: 28.6139, longitude: 77.2090 }
      updateMapMarker(location);
    });
  }
});
client.activate();
```

---

## 10. Pagination

Paginated endpoints accept query parameters:

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | 0 | Zero-indexed page number |
| `size` | int | 20 | Items per page |

Currently paginated: `GET /v1/trips/customer/{customerId}`

---

## 11. Error Handling

### Common Error Codes

| HTTP Status | Error Code | When |
|------------|-----------|------|
| 400 | `VALIDATION_ERROR` | Invalid request body / missing required fields |
| 401 | `UNAUTHORIZED` | Missing or expired JWT token |
| 403 | `ACCESS_DENIED` | Wrong role for endpoint |
| 404 | `RESOURCE_NOT_FOUND` | Entity with given ID doesn't exist |
| 409 | `BUSINESS_RULE_VIOLATION` | Invalid state transition, duplicate phone, etc. |
| 500 | `INTERNAL_ERROR` | Server error |

### Validation Messages

- `"Phone number is required"` — phoneNumber is blank
- `"Invalid Indian phone number"` — doesn't match +91 pattern

---

## 12. Frontend Pages & Features Map

Based on the backend APIs, the React frontend needs these pages/features:

### Customer App

| Page | Backend APIs Used |
|------|-------------------|
| **Login / OTP Screen** | `POST /otp/send`, `POST /otp/verify` |
| **Profile Setup** (new user) | `PUT /customers/{id}` |
| **Profile View/Edit** | `GET /customers/me`, `PUT /customers/{id}` |
| **Home / Book Driver** | `GET /locations/nearby`, fare estimation |
| **Location Picker** | Google Maps / saved locations |
| **Trip Booking** | `POST /trips` |
| **Driver Matching** | Poll `GET /trips/{id}` for status changes |
| **Active Trip** (live) | WebSocket `/topic/trip/{tripId}/location`, `GET /trips/{id}` |
| **Trip OTP Display** | Show `startOtp` from trip object |
| **Trip Complete** | Rating + feedback `POST /trips/{id}/rate` |
| **Trip History** | `GET /trips/customer/{id}?page=0&size=20` |
| **Trip Detail** | `GET /trips/{id}` |
| **Cancel Trip** | `POST /trips/{id}/cancel` |
| **Wallet** | (Wallet APIs - to be built) |
| **Support** | (Support APIs - to be built) |
| **Safety / SOS** | (Safety APIs - to be built) |
| **Saved Locations** | (via Customer's savedLocations)  |
| **Vehicles** | (Vehicle management - to be built) |
| **Settings** | Profile, language, notifications |

### Driver App

| Page | Backend APIs Used |
|------|-------------------|
| **Login / OTP Screen** | `POST /otp/send`, `POST /otp/verify` (role: DRIVER) |
| **Profile Setup** (new user) | Profile completion form |
| **Document Upload** | `POST /drivers/{id}/documents` (5 document types) |
| **Verification Status** | `GET /drivers/me` → check `status` |
| **Go Online/Offline** | `PUT /drivers/{id}/availability` |
| **Incoming Trip Request** | Listen for trip assignment notifications |
| **Active Trip** | `POST /trips/{id}/driver-arrived`, `POST /trips/{id}/start` (enter OTP) |
| **Trip Navigation** | `POST /locations/driver/{id}/trip/{tripId}` (send GPS updates) |
| **Trip Complete** | `POST /trips/{id}/complete` |
| **Earnings Dashboard** | Driver profile (`totalEarnings`, `totalTrips`) |
| **Ratings & Reviews** | `averageRating`, `totalRatings` |
| **Trip History** | Trip listing for driver (to be built) |

### Admin Panel

| Page | Backend APIs Used |
|------|-------------------|
| **Dashboard** | `GET /admin/dashboard/stats` |
| **Driver Approvals** | `POST /admin/drivers/{id}/approve`, `POST /admin/drivers/{id}/reject` |
| **Dispute Resolution** | `POST /admin/disputes/{tripId}/resolve` |
| **Driver Management** | List/search drivers, view documents |
| **Trip Management** | List/search trips |
| **Safety Incidents** | View/manage safety reports |
| **Support Tickets** | View/manage support tickets |
| **Pricing Management** | CRUD for pricing rules per city |
| **Analytics Dashboard** | Trip metrics, revenue, driver metrics |

---

## 13. Backend Connection Details

### Local Development

| Config | Value |
|--------|-------|
| **API Base URL** | `http://localhost:8080/api` |
| **WebSocket URL** | `http://localhost:8080/api/ws` |
| **Swagger UI** | `http://localhost:8080/api/swagger-ui.html` |
| **CORS** | Allowed origins: `http://localhost:3000` (React dev server) |

### CORS Configuration

The backend allows CORS from `localhost:3000` by default. If your React app runs on a different port, update `CORS_ALLOWED_ORIGINS` in the backend's `.env` file.

### API Request Headers

```
Content-Type: application/json
Authorization: Bearer <accessToken>     // for authenticated endpoints
```

### Date/Time Formats

- **Instant timestamps** in JSON: ISO-8601 format, e.g., `"2026-03-13T22:00:52.854673Z"`
- **LocalDate** in JSON: `"2026-03-13"` (YYYY-MM-DD)
- **All timestamps are UTC** — convert to IST (UTC+5:30) in the frontend

### ID Format

All entity IDs are **UUID v4** strings, e.g., `"550e8400-e29b-41d4-a716-446655440000"`

---

## Summary of What Exists vs What Needs Frontend Work

### ✅ Fully implemented in backend (build frontend for these):
- OTP authentication + JWT token management
- Customer profile CRUD
- Driver profile + availability toggle
- Document upload for driver onboarding
- Complete trip lifecycle (create → match → arrive → OTP start → complete → rate → cancel)
- Trip history with pagination
- Location tracking + WebSocket streaming
- Nearby driver search
- Admin dashboard + driver approvals + dispute resolution

### 🔄 Backend models exist but no REST endpoints yet (frontend can scaffold, endpoints coming):
- Wallet balance + transactions
- Support ticket CRUD
- Safety incident reporting
- Vehicle management
- Pricing rule management
- Notification preferences
- Driver trip history endpoint
- Analytics data endpoints

### 📝 Important notes for frontend development:
1. Store `accessToken` and `refreshToken` in secure storage (httpOnly cookies or encrypted localStorage)
2. Auto-refresh token when receiving 401 responses
3. Handle `isNewUser: true` by redirecting to profile completion
4. Poll trip status every 3-5 seconds during DRIVER_MATCHING state
5. Switch to WebSocket once trip is in DRIVER_ARRIVING or TRIP_STARTED state
6. All monetary values are in INR (₹)
7. Trip OTP is shown to customer and entered by driver — this is a verification mechanism
8. Driver documents must be uploaded one by one (separate API calls per document type)
9. The near-by drivers endpoint returns driver IDs — fetch driver details separately
10. Use the `tripNumber` (e.g., `RYD-20260313-ABCD`) for display, NOT the UUID `id`

# Rydvrse API Specification

## Document Control

- Document Name: `API_Spec.md`
- Product: `Rydvrse`
- Version: `1.0`
- Status: `Baseline API Contract for MVP`
- Last Updated: `2026-04-10`
- Source Documents:
  - `High_Level_Design.md`
  - `MVP_Scope.md`
  - `Product_Requirements_Document.md`
  - `Screen_Flow.md`
  - `Pricing_and_Payout_Design.md`
  - `Database_Schema.md`
- Intended Audience:
  - backend engineering
  - frontend and mobile engineering
  - QA and release teams
  - product and design
  - operations and support
  - finance and audit stakeholders

## 1. Purpose

This document defines the external API contract for the Rydvrse MVP.

It turns the product, pricing, flow, and schema decisions already made into a build-ready interface specification covering:

- customer APIs
- driver APIs
- admin and ops APIs
- shared request and response conventions
- authentication and authorization rules
- versioning and backward-compatibility rules
- error contracts
- webhook and callback contracts

This document is the source of truth for HTTP-facing integration behavior. If implementation differs from this document, the difference must be treated as a deliberate API decision and documented formally.

## 2. Scope

This specification covers the MVP API surface for:

- customer mobile app
- driver mobile app
- admin and ops web dashboard
- payment-provider callbacks
- shared bootstrap and upload support APIs

This document does not define:

- internal service-to-service event schemas
- database DDL
- message queue payloads
- analytics warehouse contracts
- third-party SDK implementation details

## 3. API Style and Design Principles

### 3.1 Style

- REST over HTTPS for synchronous request and response interactions
- JSON request and response bodies
- Server-Sent Events for selected live-tracking and state-update flows
- webhooks for payment-provider callbacks

### 3.2 Design Principles

- versioned APIs from day one
- resource-oriented naming
- strong request validation
- idempotent write semantics for critical mutations
- explicit lifecycle state transitions
- auditable financial and operational mutations
- snapshot-based commercial responses
- consistent error contract
- correlation IDs on every response

### 3.3 Non-Negotiable Product Rules Reflected in the API

- pricing must be visible before booking confirmation
- quote expiry must be enforced server-side
- trip billing must not start before customer start confirmation except via audited ops override
- stale driver data must not remain visible after reassignment
- refunds, overrides, manual reassignments, and approvals must be auditable

## 4. Base URLs and Environments

### 4.1 Canonical Base Paths

- Production: `https://api.rydvrse.com/api/v1`
- Staging: `https://staging-api.rydvrse.com/api/v1`
- Local development: `http://localhost:8080/api/v1`

### 4.2 Version Path Rule

- All externally supported endpoints must live under `/api/v1`
- Admin APIs also live under the same version root, for example `/api/v1/admin/bookings`
- Webhooks may use `/api/v1/webhooks/...`

## 5. Common Protocol Conventions

### 5.1 Transport

- HTTPS only in non-local environments
- `Content-Type: application/json`
- `Accept: application/json`
- UTF-8 encoding

### 5.2 Naming

- request and response field names use `snake_case`
- resource identifiers use UUID strings
- timestamps use ISO 8601 strings with timezone, stored and returned in UTC

### 5.3 Money

- all money amounts are integers in `paise`
- no floating-point money fields are allowed
- currency is `INR` for MVP

Example:

```json
{
  "amount_paise": 44900,
  "currency": "INR"
}
```

### 5.4 Location and Coordinates

- latitude and longitude are decimal numbers
- the API accepts either a resolved address object, a coordinate pair, or both
- geo resolution performed by the backend must not silently change the user-visible address without returning the resolved version

### 5.5 Standard Headers

| Header | Required | Applies To | Purpose |
|---|---|---|---|
| `Authorization` | Yes for protected endpoints | all protected endpoints | bearer access token |
| `X-Request-Id` | Recommended on request, always returned in response | all endpoints | correlation and debugging |
| `Idempotency-Key` | Required on critical write endpoints | booking, payment, cancel, admin financial and rescue actions | safe retries |
| `X-Client-Platform` | Recommended | mobile and web app calls | `IOS`, `ANDROID`, `WEB` |
| `X-App-Version` | Recommended | mobile and web app calls | compatibility and support |
| `X-Device-Id` | Recommended | auth, tracking, support | device traceability |
| `X-Client-Timezone` | Recommended | quote, booking, admin dashboards | client-local presentation context |
| `Accept-Language` | Recommended | customer and driver endpoints | localization support |

### 5.6 Standard Status Codes

| Status | Meaning |
|---|---|
| `200 OK` | successful read or mutation with body |
| `201 Created` | resource created |
| `202 Accepted` | async work accepted |
| `204 No Content` | successful request with no body |
| `400 Bad Request` | malformed request or unsupported input |
| `401 Unauthorized` | missing or invalid auth |
| `403 Forbidden` | authenticated but not allowed |
| `404 Not Found` | resource not found or not visible to actor |
| `409 Conflict` | state conflict, idempotency conflict, stale version |
| `422 Unprocessable Entity` | validation or business-rule failure |
| `429 Too Many Requests` | rate limit triggered |
| `500 Internal Server Error` | unexpected server failure |
| `503 Service Unavailable` | dependency or maintenance issue |

## 6. Authentication and Authorization

### 6.1 Actor Types

Rydvrse supports three top-level actor classes at the API layer:

- `CUSTOMER`
- `DRIVER`
- `ADMIN`

### 6.2 Session Model

- access token: JWT bearer token
- refresh token: opaque rotating token
- each successful login creates a session record
- each refresh invalidates the previous refresh token for that session

Recommended TTLs for MVP:

- customer and driver access token: `15 minutes`
- customer and driver refresh token: `30 days`, rolling
- admin access token: `15 minutes`
- admin refresh token: `8 hours`, rolling

### 6.3 Access Token Claims

The JWT access token should include at minimum:

- `sub`: user account ID
- `session_id`
- `actor_type`
- `roles`: array, required for admin
- `scopes`: optional flattened permissions
- `iat`
- `exp`

### 6.4 Mobile Actor Authentication

Customers and drivers use OTP authentication.

Flow:

1. request OTP
2. verify OTP
3. receive access and refresh tokens
4. use refresh endpoint until logout or session expiry

### 6.5 Admin Authentication

Admin users use credential-based authentication with role binding.

MVP contract:

- `email + password` login is required
- second factor is strongly recommended for production and can be introduced without breaking the path structure
- admin accounts never use customer or driver OTP endpoints

### 6.6 Authorization Rules

- customer tokens may access only customer routes and only customer-owned resources
- driver tokens may access only driver routes and only resources bound to that driver
- admin tokens may access only routes allowed by bound admin roles
- no cross-actor token sharing is allowed
- tokens for suspended users or drivers must be rejected

### 6.7 Admin Role Baseline

| Role | Route Groups |
|---|---|
| `OPS_EXECUTIVE` | dashboard read, bookings read, rescue queue read, manual reassign, support read/update |
| `SUPPORT_SPECIALIST` | support read/update, incident read/update, booking read, limited refunds |
| `DRIVER_REVIEWER` | driver onboarding review and approval routes |
| `FINANCE_ADMIN` | refunds, payment review, payout reporting |
| `CITY_MANAGER` | pricing read/write, serviceability read/write, operational reports |
| `SUPER_ADMIN` | all admin endpoints |

### 6.8 Ownership Rules

- customer ownership is determined by `booking.customer_id`
- driver ownership is determined by current or historical assignment binding
- admin actors may read across ownership boundaries only when their role allows it
- resource visibility should return `404` instead of leaking unauthorized existence where appropriate

## 7. Versioning and Compatibility Rules

### 7.1 Versioning Strategy

- major version in URI path, for example `/api/v1`
- additive non-breaking changes remain within the same major version
- breaking changes require `/api/v2`

### 7.2 What Counts as Non-Breaking

- adding optional response fields
- adding new endpoints
- adding new filter parameters
- adding new enum values when clients are expected to ignore unknown values safely

### 7.3 What Counts as Breaking

- removing or renaming fields
- changing field types
- changing auth requirements on an existing endpoint
- changing response envelope shape
- changing semantic meaning of an existing field

### 7.4 Deprecation Policy

- deprecated endpoints or fields must be announced before removal
- target deprecation window: `90 days`
- responses for deprecated endpoints should include:
  - `Deprecation: true`
  - `Sunset: <RFC 1123 date>`

### 7.5 Client Compatibility Rules

- clients must ignore unknown response fields
- clients must handle unknown enum values safely
- server must reject unknown top-level request fields with a validation error to avoid silent mistakes

## 8. Common Response Envelope

### 8.1 Success Envelope

All successful JSON responses use:

```json
{
  "data": {},
  "meta": {
    "request_id": "7c58ab2d-3189-4ab5-a26a-0c16c511c090",
    "timestamp": "2026-04-10T09:12:41Z",
    "api_version": "v1"
  }
}
```

### 8.2 List Envelope

```json
{
  "data": [],
  "meta": {
    "request_id": "7c58ab2d-3189-4ab5-a26a-0c16c511c090",
    "timestamp": "2026-04-10T09:12:41Z",
    "api_version": "v1",
    "pagination": {
      "limit": 20,
      "next_cursor": "eyJsYXN0X2lkIjoiLi4uIn0="
    }
  }
}
```

### 8.3 Empty Success

For `204 No Content`, the server returns no JSON body.

## 9. Error Contract

### 9.1 Error Envelope

```json
{
  "error": {
    "code": "QUOTE_EXPIRED",
    "message": "The quote has expired. Request a fresh quote before booking.",
    "type": "business_rule_violation",
    "retryable": false,
    "details": [
      {
        "field": "quote_id",
        "reason": "expired"
      }
    ]
  },
  "meta": {
    "request_id": "7c58ab2d-3189-4ab5-a26a-0c16c511c090",
    "timestamp": "2026-04-10T09:12:41Z",
    "api_version": "v1"
  }
}
```

### 9.2 Error Types

- `validation_error`
- `authentication_error`
- `authorization_error`
- `not_found`
- `conflict`
- `business_rule_violation`
- `rate_limit`
- `dependency_failure`
- `server_error`

### 9.3 Standard Error Codes

| Code | Typical Status | Meaning |
|---|---|---|
| `VALIDATION_ERROR` | `422` | request body or query invalid |
| `UNKNOWN_FIELD` | `400` | request contains unsupported field |
| `UNAUTHORIZED` | `401` | missing or invalid token |
| `SESSION_EXPIRED` | `401` | refresh required |
| `FORBIDDEN` | `403` | route not allowed for actor or role |
| `RESOURCE_NOT_FOUND` | `404` | requested entity not found |
| `STATE_CONFLICT` | `409` | invalid state transition |
| `STALE_ROW_VERSION` | `409` | resource changed since client last read |
| `RATE_LIMITED` | `429` | request limit exceeded |
| `DEPENDENCY_UNAVAILABLE` | `503` | external dependency unavailable |

### 9.4 Domain Error Codes

| Code | Domain |
|---|---|
| `AUTH_OTP_RATE_LIMITED` | auth |
| `AUTH_OTP_INVALID` | auth |
| `AUTH_OTP_EXPIRED` | auth |
| `AUTH_ACCOUNT_SUSPENDED` | auth |
| `SERVICEABILITY_UNAVAILABLE` | quote and booking |
| `QUOTE_EXPIRED` | quote and booking |
| `QUOTE_NOT_BOOKABLE` | quote and booking |
| `BOOKING_MODIFICATION_NOT_ALLOWED` | booking |
| `BOOKING_CANCELLATION_NOT_ALLOWED` | booking |
| `BOOKING_ALREADY_CANCELLED` | booking |
| `ASSIGNMENT_OFFER_EXPIRED` | assignment |
| `ASSIGNMENT_ALREADY_ACCEPTED` | assignment |
| `DRIVER_NOT_APPROVED` | driver |
| `DRIVER_DOCUMENT_INCOMPLETE` | driver |
| `TRIP_START_CONFIRMATION_NOT_ALLOWED` | trip |
| `TRIP_ALREADY_COMPLETED` | trip |
| `PAYMENT_ORDER_CREATION_FAILED` | payments |
| `PAYMENT_ALREADY_CAPTURED` | payments |
| `REFUND_NOT_ALLOWED` | refunds |
| `OVERRIDE_LIMIT_EXCEEDED` | admin |
| `IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD` | common |

## 10. Common Query, Pagination, and Idempotency Rules

### 10.1 Pagination

- list endpoints use cursor pagination
- query parameters:
  - `limit`
  - `cursor`
- default `limit`: `20`
- max `limit`: `100`

### 10.2 Filtering

Common filter parameters where supported:

- `state`
- `service_type`
- `from`
- `to`
- `city_id`
- `zone_id`
- `q` for admin search only

### 10.3 Sorting

Supported parameters:

- `sort_by`
- `sort_order`

Default sort:

- descending by newest relevant timestamp

### 10.4 Idempotency

`Idempotency-Key` is required for:

- booking creation
- booking modification apply
- booking cancellation
- trip start confirmation
- payment order creation
- support ticket creation
- driver assignment accept and decline
- driver trip completion
- admin reassignment
- admin cancellation
- admin refund creation
- admin approval and rejection actions

Idempotency behavior:

- key scope is `endpoint + actor + normalized request payload`
- replay of same key with same payload returns the original result
- replay of same key with different payload returns `409 IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD`

### 10.5 Optimistic Concurrency

For high-risk admin mutations, request bodies must include `row_version` of the target resource or sub-resource when applicable.

If the backend detects a stale version, it must return `409 STALE_ROW_VERSION`.

## 11. Shared Enumerations and State Models

### 11.1 Service Types

- `SCHEDULED_LOCAL`
- `SCHEDULED_ONE_WAY`
- `SCHEDULED_ROUND_TRIP`
- `AIRPORT`
- `LATE_NIGHT`

### 11.2 Booking States

- `CONFIRMED_PENDING_ASSIGNMENT`
- `ASSIGNMENT_AT_RISK`
- `ASSIGNED`
- `DRIVER_ARRIVED`
- `TRIP_START_PENDING`
- `IN_PROGRESS`
- `COMPLETED_PAYMENT_PENDING`
- `COMPLETED`
- `CANCELLED`
- `FAILED_FULFILLMENT`

### 11.3 Assignment States

- `OFFERED`
- `ACCEPTED`
- `DECLINED`
- `EXPIRED`
- `LOCKED`
- `CANCELLED`
- `REASSIGNED`

### 11.4 Trip States

- `NOT_STARTED`
- `ARRIVED`
- `START_PENDING`
- `IN_PROGRESS`
- `COMPLETED`
- `INTERRUPTED`

### 11.5 Payment States

- `PENDING`
- `AUTHORIZED`
- `CAPTURED`
- `FAILED`
- `REFUNDED`
- `PARTIALLY_REFUNDED`

### 11.6 Refund States

- `REQUESTED`
- `APPROVED`
- `REJECTED`
- `PROCESSING`
- `PROCESSED`
- `FAILED`

### 11.7 Driver Onboarding States

- `INCOMPLETE`
- `SUBMITTED`
- `UNDER_REVIEW`
- `APPROVED`
- `REJECTED`
- `CORRECTION_REQUIRED`
- `SUSPENDED`

## 12. Shared Resource Models

The following models are reused across endpoint groups.

### 12.1 `money`

| Field | Type | Required | Notes |
|---|---|---|---|
| `amount_paise` | integer | Yes | non-negative unless explicitly used for delta/adjustment |
| `currency` | string | Yes | `INR` for MVP |

### 12.2 `location`

| Field | Type | Required | Notes |
|---|---|---|---|
| `label` | string | No | user-facing short label |
| `address_line_1` | string | Yes | resolved primary address |
| `address_line_2` | string | No | secondary address |
| `landmark` | string | No | optional landmark |
| `city_id` | string | Yes | service city identifier |
| `zone_id` | string | No | resolved service zone |
| `latitude` | number | Yes | decimal latitude |
| `longitude` | number | Yes | decimal longitude |
| `place_id` | string | No | mapping-provider place reference |

### 12.3 `quote_component`

| Field | Type | Required | Notes |
|---|---|---|---|
| `code` | string | Yes | stable component code |
| `label` | string | Yes | display label |
| `amount_paise` | integer | Yes | charge amount |
| `is_tax` | boolean | Yes | whether component is tax |
| `metadata` | object | No | band, plan version, notes |

### 12.4 `quote`

| Field | Type | Required | Notes |
|---|---|---|---|
| `quote_id` | string | Yes | UUID |
| `status` | string | Yes | `ACTIVE`, `EXPIRED`, `NOT_BOOKABLE` |
| `service_type` | string | Yes | one of service types |
| `pickup` | `location` | Yes | pickup snapshot |
| `drop` | `location` | No | null for some local flows |
| `scheduled_pickup_at` | string | Yes | UTC timestamp |
| `expected_duration_minutes` | integer | No | required for time-based services |
| `lead_time_bucket` | string | Yes | `FLEX`, `PRIORITY`, `EXPRESS` |
| `pricing_plan_version` | string | Yes | immutable commercial version |
| `commercial_model` | string | Yes | pricing algorithm/model code, e.g. `BLR_HYBRID_ONE_WAY_V1` |
| `pricing_assumptions` | object | Yes | route, pickup-acquisition, vehicle, and estimate-quality inputs used by the quote |
| `components` | array | Yes | quote breakdown |
| `subtotal_paise` | integer | Yes | pre-tax or pre-total according to plan |
| `tax_paise` | integer | Yes | tax amount |
| `total_paise` | integer | Yes | total amount payable |
| `driver_payout_preview` | object | No | internal/future-driver preview of earning components; not shown to customers unless product enables it |
| `savings_summary` | object | No | customer-friendly comparison against configured reference model when available |
| `valid_until` | string | Yes | quote expiry |
| `cancellation_policy_summary` | object | Yes | summary text and key thresholds |
| `serviceability_status` | string | Yes | `SERVICEABLE` or reasoned non-serviceable state |

### 12.5 `driver_public_profile`

| Field | Type | Required | Notes |
|---|---|---|---|
| `driver_id` | string | Yes | UUID |
| `first_name` | string | Yes | public-safe |
| `photo_url` | string | No | signed or CDN URL |
| `rating` | number | No | nullable for new drivers |
| `rating_count` | integer | No | nullable |
| `verification_badges` | array | Yes | `KYC_VERIFIED`, `TRAINED`, `BACKGROUND_CHECKED` |
| `languages` | array | Yes | language codes or labels |
| `masked_mobile_number` | string | No | masked contact |
| `punctuality_score` | number | No | optional trust signal |

### 12.6 `assignment_summary`

| Field | Type | Required | Notes |
|---|---|---|---|
| `assignment_id` | string | Yes | UUID |
| `state` | string | Yes | assignment state |
| `driver` | `driver_public_profile` | No | present once locked |
| `eta_minutes` | integer | No | pickup ETA |
| `assigned_at` | string | No | when assignment locked |
| `arrived_at` | string | No | when driver marked arrived |
| `reassignment_count` | integer | Yes | number of completed reassignments |

### 12.7 `booking_summary`

| Field | Type | Required | Notes |
|---|---|---|---|
| `booking_id` | string | Yes | UUID |
| `booking_number` | string | Yes | user-facing ID |
| `state` | string | Yes | booking state |
| `service_type` | string | Yes | service type |
| `scheduled_pickup_at` | string | Yes | UTC timestamp |
| `pickup` | `location` | Yes | pickup snapshot |
| `drop` | `location` | No | drop snapshot |
| `total_paise` | integer | Yes | locked fare total |
| `payment_state` | string | Yes | current payment state |
| `assignment` | `assignment_summary` | No | null when not yet assigned |

### 12.8 `booking_detail`

Extends `booking_summary` with:

- `quote_snapshot`
- `fare_components`
- `timeline`
- `customer_notes`
- `trip_start_required`
- `modification_allowed`
- `cancellation_allowed`
- `cancellation_preview`
- `support_summary`
- `trip_id`
- `invoice_id`

### 12.9 `tracking_snapshot`

| Field | Type | Required | Notes |
|---|---|---|---|
| `trip_id` | string | Yes | UUID |
| `trip_state` | string | Yes | trip state |
| `driver_location` | object | No | live or last-known position |
| `route_polyline` | string | No | encoded path |
| `eta_minutes` | integer | No | ETA to destination or pickup |
| `updated_at` | string | Yes | last refresh |
| `share_url` | string | No | active share link if generated |

### 12.10 `payment_order`

| Field | Type | Required | Notes |
|---|---|---|---|
| `payment_id` | string | Yes | UUID |
| `booking_id` | string | Yes | UUID |
| `state` | string | Yes | payment state |
| `provider` | string | Yes | for MVP likely `RAZORPAY` or equivalent |
| `provider_order_id` | string | Yes | provider-side order reference |
| `amount_paise` | integer | Yes | payable amount |
| `payment_method` | string | Yes | `UPI_INTENT`, `UPI_COLLECT`, `CARD` if enabled later |
| `expires_at` | string | No | if provider order expires |

### 12.11 `support_ticket`

| Field | Type | Required | Notes |
|---|---|---|---|
| `ticket_id` | string | Yes | UUID |
| `state` | string | Yes | `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED` |
| `category` | string | Yes | issue category |
| `sub_category` | string | No | deeper issue type |
| `severity` | string | Yes | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `description` | string | Yes | user-entered issue |
| `booking_id` | string | No | attached context |
| `trip_id` | string | No | attached context |
| `created_at` | string | Yes | created timestamp |
| `last_updated_at` | string | Yes | last activity timestamp |

### 12.12 `payout_preview`

| Field | Type | Required | Notes |
|---|---|---|---|
| `assignment_id` | string | Yes | UUID |
| `payout_plan_version` | string | Yes | immutable payout version |
| `components` | array | Yes | payout breakdown |
| `total_payout_paise` | integer | Yes | estimated or locked payout |
| `notes` | array | No | explanatory notes for driver |

## 13. Shared Bootstrap and Utility APIs

### 13.1 `GET /api/v1/config/bootstrap`

Purpose:

- return client bootstrapping information needed at app start

Auth:

- optional

Query Parameters:

| Field | Type | Required | Notes |
|---|---|---|---|
| `actor_type` | string | No | `CUSTOMER`, `DRIVER`, `ADMIN` |
| `city_id` | string | No | optional context |

Response `200`:

- app minimum supported version
- supported service types
- supported payment methods
- emergency/support contact numbers
- active city configuration
- policy summaries needed before login or booking

### 13.2 `POST /api/v1/config/serviceability/check`

Purpose:

- allow the app to validate whether a pickup and optional drop are serviceable before full quote creation

Auth:

- optional for customer

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `service_type` | string | Yes | requested service type |
| `pickup` | `location` | Yes | pickup input |
| `drop` | `location` | No | drop input |
| `scheduled_pickup_at` | string | Yes | requested time |

Response `200`:

- `serviceable`
- `reason_code`
- `resolved_pickup_zone_id`
- `resolved_drop_zone_id`

### 13.3 `POST /api/v1/media/upload-requests`

Purpose:

- create a pre-signed upload request for documents, support attachments, or handover photos

Auth:

- customer, driver, or admin

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `purpose` | string | Yes | `DRIVER_DOCUMENT`, `SUPPORT_ATTACHMENT`, `HANDOVER_PHOTO` |
| `file_name` | string | Yes | original name |
| `content_type` | string | Yes | MIME type |
| `size_bytes` | integer | Yes | size for validation |

Response `201`:

- `media_asset_id`
- `upload_url`
- `expires_at`
- `headers`

### 13.4 `POST /api/v1/media/{media_asset_id}/complete`

Purpose:

- finalize a media upload after the client successfully uploads to object storage

Auth:

- customer, driver, or admin

Response `200`:

- `media_asset_id`
- `state`
- `download_url`

## 14. Public Auth APIs

### 14.1 `POST /api/v1/auth/otp/request`

Purpose:

- request an OTP for customer or driver login

Auth:

- none

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `mobile_number` | string | Yes | normalized Indian number |
| `actor_type` | string | Yes | `CUSTOMER` or `DRIVER` |
| `purpose` | string | Yes | `LOGIN` |
| `device_id` | string | No | recommended |

Response `200`:

| Field | Type | Notes |
|---|---|---|
| `challenge_id` | string | identifier for verify call |
| `masked_mobile_number` | string | display-safe |
| `expires_at` | string | OTP expiry |
| `retry_after_seconds` | integer | resend cooldown |

Failure Cases:

- `AUTH_OTP_RATE_LIMITED`
- `VALIDATION_ERROR`
- `AUTH_ACCOUNT_SUSPENDED`

### 14.2 `POST /api/v1/auth/otp/verify`

Purpose:

- verify OTP and create an authenticated customer or driver session

Auth:

- none

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `challenge_id` | string | Yes | from OTP request |
| `otp_code` | string | Yes | one-time code |
| `actor_type` | string | Yes | must match request |
| `device_id` | string | No | recommended |

Response `200`:

```json
{
  "data": {
    "access_token": "<jwt>",
    "refresh_token": "<opaque>",
    "expires_in_seconds": 900,
    "actor_type": "CUSTOMER",
    "user": {
      "user_id": "4c0e4297-7e69-43e8-b8bf-b02cc65d633d",
      "mobile_number": "+919999999999"
    },
    "profile": {
      "profile_id": "fb89ea29-cfa8-4f16-a1a3-1e3481c5b36a",
      "is_new_user": true,
      "onboarding_state": "INCOMPLETE"
    }
  },
  "meta": {
    "request_id": "7c58ab2d-3189-4ab5-a26a-0c16c511c090",
    "timestamp": "2026-04-10T09:12:41Z",
    "api_version": "v1"
  }
}
```

Failure Cases:

- `AUTH_OTP_INVALID`
- `AUTH_OTP_EXPIRED`
- `AUTH_ACCOUNT_SUSPENDED`

### 14.3 `POST /api/v1/auth/refresh`

Purpose:

- exchange a valid refresh token for a new access token and rotated refresh token

Auth:

- none, refresh token in body

Request Fields:

- `refresh_token`
- `device_id` optional but recommended

Response `200`:

- same token payload shape as OTP verify response

### 14.4 `POST /api/v1/auth/logout`

Purpose:

- invalidate the current session

Auth:

- bearer token required

Request Fields:

- `refresh_token` optional if server already binds current session

Response:

- `204 No Content`

## 15. Customer APIs

### 15.1 Customer API Domain Summary

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/customers/me` | get customer profile |
| `PATCH` | `/api/v1/customers/me` | update customer profile |
| `GET` | `/api/v1/customers/me/home` | home summary |
| `GET` | `/api/v1/customers/me/saved-locations` | list saved locations |
| `POST` | `/api/v1/customers/me/saved-locations` | create saved location |
| `PATCH` | `/api/v1/customers/me/saved-locations/{saved_location_id}` | update saved location |
| `DELETE` | `/api/v1/customers/me/saved-locations/{saved_location_id}` | soft delete saved location |
| `POST` | `/api/v1/quotes` | create quote |
| `GET` | `/api/v1/quotes/{quote_id}` | get quote |
| `POST` | `/api/v1/bookings` | create booking |
| `GET` | `/api/v1/bookings` | list bookings |
| `GET` | `/api/v1/bookings/{booking_id}` | get booking detail |
| `POST` | `/api/v1/bookings/{booking_id}/modification-preview` | preview edit impact |
| `POST` | `/api/v1/bookings/{booking_id}/modify` | apply booking edit |
| `GET` | `/api/v1/bookings/{booking_id}/cancellation-preview` | preview cancellation outcome |
| `POST` | `/api/v1/bookings/{booking_id}/cancel` | cancel booking |
| `POST` | `/api/v1/bookings/{booking_id}/start-confirmation` | confirm trip start |
| `GET` | `/api/v1/trips/{trip_id}` | get trip detail |
| `GET` | `/api/v1/trips/{trip_id}/tracking` | get live tracking snapshot |
| `GET` | `/api/v1/trips/{trip_id}/tracking/stream` | SSE trip updates |
| `POST` | `/api/v1/trips/{trip_id}/share-links` | create live-share link |
| `POST` | `/api/v1/trips/{trip_id}/sos` | create SOS event |
| `POST` | `/api/v1/bookings/{booking_id}/payment-orders` | create payment order |
| `GET` | `/api/v1/payments/{payment_id}` | get payment state |
| `GET` | `/api/v1/bookings/{booking_id}/invoice` | get invoice |
| `POST` | `/api/v1/bookings/{booking_id}/ratings` | submit rating |
| `POST` | `/api/v1/support/tickets` | create support ticket |
| `GET` | `/api/v1/support/tickets` | list own tickets |
| `GET` | `/api/v1/support/tickets/{ticket_id}` | get own ticket detail |

All customer endpoints require:

- `Authorization: Bearer <customer access token>`
- actor type `CUSTOMER`

### 15.2 `GET /api/v1/customers/me`

Response `200` includes:

- `customer_id`
- `full_name`
- `email`
- `mobile_number`
- `city_id`
- `profile_completion_state`
- `default_saved_location_id`
- `created_at`

### 15.3 `PATCH /api/v1/customers/me`

Allowed Fields:

- `full_name`
- `email`
- `city_id`

Rejected Fields:

- `mobile_number`
- `user_id`
- any financial or audit fields

Response:

- updated customer profile

### 15.4 `GET /api/v1/customers/me/home`

Purpose:

- home-screen payload to reduce app chattiness

Response includes:

- customer profile summary
- supported service types for city
- upcoming bookings summary
- recent bookings summary
- help and safety quick actions

### 15.5 Saved Locations APIs

Create request fields:

- `label`
- `location`
- `is_default`

Update request fields:

- `label`
- `location`
- `is_default`

Delete behavior:

- soft delete only
- return `204`

### 15.6 `POST /api/v1/quotes`

Purpose:

- create a price quote from booking inputs

Idempotency:

- optional but recommended

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `service_type` | string | Yes | service type |
| `pickup` | `location` | Yes | pickup |
| `drop` | `location` | No | required for one-way and airport |
| `scheduled_pickup_at` | string | Yes | UTC time |
| `expected_duration_minutes` | integer | No | required for local and round trip |
| `rounded_distance_km` | integer | No | required for Bengaluru one-way and round trip when map estimate is available; backend rounds up if exact distance exists in future |
| `predicted_drive_minutes` | integer | No | traffic-aware route ETA used by hybrid pricing |
| `driver_pickup_distance_km` | integer | No | estimated nearest eligible driver distance to pickup |
| `driver_pickup_eta_minutes` | integer | No | estimated driver arrival time; target is `<= 30` for standard Bengaluru fulfillment |
| `estimated_pickup_cost_paise` | integer | No | optional acquisition cost override; backend estimates if absent |
| `transmission_type` | string | No | `MANUAL`, `AUTOMATIC` |
| `car_type` | string | No | `HATCHBACK`, `SEDAN`, `SUV`, `LUXURY` |
| `car_brand_model` | string | No | customer-entered vehicle model for matching context |
| `car_number` | string | No | customer-entered registration number for driver verification |
| `round_trip_wait_minutes` | integer | No | planned waiting time for round trips |
| `safety_addon_opted` | boolean | No | whether optional safety/secure fee is included; default `true` for launch quote UI |
| `customer_notes` | string | No | not used for pricing |
| `lead_time_bucket_override` | string | No | admin only, reject for customers |

Response `201`:

- `quote`

Failure Cases:

- `SERVICEABILITY_UNAVAILABLE`
- `VALIDATION_ERROR`

Example:

```json
{
  "data": {
    "quote_id": "a0714877-0732-4684-a6ce-6d093220db13",
    "status": "ACTIVE",
    "service_type": "SCHEDULED_ONE_WAY",
    "pickup": {
      "label": "Home",
      "address_line_1": "Koramangala 5th Block",
      "city_id": "blr",
      "zone_id": "blr_core",
      "latitude": 12.9352,
      "longitude": 77.6245
    },
    "drop": null,
    "scheduled_pickup_at": "2026-04-11T13:30:00Z",
    "expected_duration_minutes": 105,
    "lead_time_bucket": "FLEX",
    "pricing_plan_version": "blr-v1.0.0",
    "commercial_model": "BLR_HYBRID_ONE_WAY_V1",
    "pricing_assumptions": {
      "rounded_distance_km": 31,
      "predicted_drive_minutes": 105,
      "included_distance_km": 20,
      "included_minutes": 99,
      "driver_pickup_distance_km": 8,
      "driver_pickup_eta_minutes": 24,
      "pickup_arrival_sla_minutes": 30,
      "transmission_type": "AUTOMATIC",
      "car_type": "SEDAN",
      "estimate_quality": "CLIENT_OR_MAP_ESTIMATE"
    },
    "components": [
      {
        "code": "BLR_ONE_WAY_BASE",
        "label": "Base fare: first 20 km + 75 min",
        "amount_paise": 29900,
        "is_tax": false
      },
      {
        "code": "BLR_DISTANCE_20_35",
        "label": "Distance fee: 11 km x ₹6.50",
        "amount_paise": 7150,
        "is_tax": false
      },
      {
        "code": "BLR_TRAFFIC_TIME",
        "label": "Traffic time buffer",
        "amount_paise": 1000,
        "is_tax": false
      },
      {
        "code": "BLR_PICKUP_ACCESS",
        "label": "Driver pickup access",
        "amount_paise": 4900,
        "is_tax": false
      },
      {
        "code": "BLR_ONE_WAY_RELOCATION",
        "label": "One-way relocation allowance",
        "amount_paise": 5900,
        "is_tax": false
      },
      {
        "code": "RYD_SECURE",
        "label": "Rydvrse Secure",
        "amount_paise": 1200,
        "is_tax": false
      },
      {
        "code": "GST",
        "label": "Tax",
        "amount_paise": 9009,
        "is_tax": true
      }
    ],
    "subtotal_paise": 50050,
    "tax_paise": 9009,
    "total_paise": 59059,
    "driver_payout_preview": {
      "total_payout_paise": 36920,
      "components": [
        {
          "code": "DRIVER_BASE",
          "label": "Driver base payout",
          "amount_paise": 21000
        },
        {
          "code": "DISTANCE_PAYOUT",
          "label": "Distance payout",
          "amount_paise": 4400
        },
        {
          "code": "TIME_PAYOUT",
          "label": "Traffic-time payout",
          "amount_paise": 720
        },
        {
          "code": "PICKUP_ACCESS_PAYOUT",
          "label": "Pickup access pass-through",
          "amount_paise": 4900
        },
        {
          "code": "RELOCATION_PAYOUT",
          "label": "One-way relocation pass-through",
          "amount_paise": 5900
        }
      ]
    },
    "savings_summary": {
      "reference_total_paise": 66400,
      "estimated_savings_paise": 7350,
      "message": "Estimated lower than the reference one-way model for a 31 km Bengaluru daytime trip."
    },
    "valid_until": "2026-04-10T09:18:41Z",
    "cancellation_policy_summary": {
      "free_until": "2026-04-11T12:30:00Z",
      "late_cancel_fee_paise": 7900
    },
    "serviceability_status": "SERVICEABLE"
  },
  "meta": {
    "request_id": "7c58ab2d-3189-4ab5-a26a-0c16c511c090",
    "timestamp": "2026-04-10T09:12:41Z",
    "api_version": "v1"
  }
}
```

### 15.7 `GET /api/v1/quotes/{quote_id}`

Purpose:

- fetch a previously created quote and current status

Rules:

- quote visibility limited to owning customer
- expired quote remains readable for UX continuity but not bookable

### 15.8 `POST /api/v1/bookings`

Purpose:

- confirm a bookable quote and create a booking

Idempotency:

- required

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `quote_id` | string | Yes | active quote |
| `contact_name` | string | No | defaults to customer name |
| `contact_mobile_number` | string | No | defaults to customer mobile |
| `customer_notes` | string | No | ride instructions |
| `passenger_name` | string | No | if booking for another person |
| `passenger_mobile_number` | string | No | masked to driver if used |
| `handover_note` | string | No | visible before start |

Response `201`:

- `booking_detail`

Failure Cases:

- `QUOTE_EXPIRED`
- `QUOTE_NOT_BOOKABLE`
- `SERVICEABILITY_UNAVAILABLE`

### 15.9 `GET /api/v1/bookings`

Query Parameters:

- `state`
- `service_type`
- `from`
- `to`
- `limit`
- `cursor`

Response:

- list of `booking_summary`

### 15.10 `GET /api/v1/bookings/{booking_id}`

Purpose:

- single source of truth for customer booking status before, during, and after execution

Response includes:

- `booking_detail`
- current assignment
- current trip if any
- cancellation preview if still cancellable
- modification flags
- payment summary
- support summary

### 15.11 `POST /api/v1/bookings/{booking_id}/modification-preview`

Purpose:

- preview the commercial and policy impact of modifying a booking

Rules:

- available only when booking state and timing rules allow modification
- returns a fresh quote-like commercial preview without mutating the booking

Request Fields:

- `scheduled_pickup_at` optional
- `pickup` optional
- `drop` optional
- `expected_duration_minutes` optional
- `customer_notes` optional

Response `200`:

- `current_booking`
- `proposed_quote`
- `fare_delta_paise`
- `policy_notes`
- `modification_allowed`

Failure Cases:

- `BOOKING_MODIFICATION_NOT_ALLOWED`

### 15.12 `POST /api/v1/bookings/{booking_id}/modify`

Purpose:

- apply an allowed booking modification

Idempotency:

- required

Request Fields:

- same fields as modification preview
- `preview_quote_id` required if fare or timing changes are involved

Response `200`:

- updated `booking_detail`

### 15.13 `GET /api/v1/bookings/{booking_id}/cancellation-preview`

Purpose:

- show cancellation eligibility, fee, and expected outcome before cancel confirmation

Response `200`:

- `allowed`
- `fee_paise`
- `driver_compensation_paise`
- `refund_estimate_paise`
- `reason_summary`

### 15.14 `POST /api/v1/bookings/{booking_id}/cancel`

Purpose:

- cancel an eligible booking before trip completion

Idempotency:

- required

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `reason_code` | string | Yes | selected customer reason |
| `reason_note` | string | No | optional details |

Response `200`:

- updated `booking_detail`
- cancellation outcome
- refund summary if applicable

Failure Cases:

- `BOOKING_CANCELLATION_NOT_ALLOWED`
- `BOOKING_ALREADY_CANCELLED`

### 15.15 `POST /api/v1/bookings/{booking_id}/start-confirmation`

Purpose:

- confirm that the driver has arrived and the trip may begin billing

Idempotency:

- required

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `confirm_start` | boolean | Yes | must be `true` |
| `fuel_level_note` | string | No | optional handover note |
| `damage_note` | string | No | optional handover note |
| `media_asset_ids` | array | No | handover photos if captured |

Response `200`:

- updated `booking_detail`
- `trip_id`
- trip state `IN_PROGRESS`

Failure Cases:

- `TRIP_START_CONFIRMATION_NOT_ALLOWED`

### 15.16 `GET /api/v1/trips/{trip_id}`

Purpose:

- fetch trip detail for in-progress or completed ride

Response includes:

- trip state
- booking summary
- driver profile
- timeline
- fare summary
- safety actions summary

### 15.17 `GET /api/v1/trips/{trip_id}/tracking`

Purpose:

- provide the latest live tracking snapshot

Response:

- `tracking_snapshot`

### 15.18 `GET /api/v1/trips/{trip_id}/tracking/stream`

Purpose:

- SSE endpoint for live tracking and state changes

Auth:

- customer token required

Event Types:

- `trip.location_updated`
- `trip.state_changed`
- `assignment.reassigned`
- `payment.state_changed`

### Event Payload Shape

```json
{
  "event_type": "trip.state_changed",
  "occurred_at": "2026-04-10T09:12:41Z",
  "payload": {
    "trip_id": "8b4022bb-db53-4ce6-8f2c-2b2e0bf22ae0",
    "state": "IN_PROGRESS"
  }
}
```

### 15.19 `POST /api/v1/trips/{trip_id}/share-links`

Purpose:

- create a secure live-share link for the active trip

Request Fields:

- `expires_in_minutes`

Response `201`:

- `share_url`
- `expires_at`

### 15.20 `POST /api/v1/trips/{trip_id}/sos`

Purpose:

- create an emergency escalation event tied to the trip

Idempotency:

- required

Request Fields:

- `trigger_source` such as `CUSTOMER_APP`
- `message` optional
- `current_location` optional

Response `202`:

- `incident_id`
- `escalation_state`

### 15.21 `POST /api/v1/bookings/{booking_id}/payment-orders`

Purpose:

- create a payment order for an unpaid completed booking

Idempotency:

- required

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `payment_method` | string | Yes | `UPI_INTENT` or `UPI_COLLECT` |

Response `201`:

- `payment_order`

Failure Cases:

- `PAYMENT_ORDER_CREATION_FAILED`
- `STATE_CONFLICT`

### 15.22 `GET /api/v1/payments/{payment_id}`

Purpose:

- poll latest payment state after provider interaction

Response:

- `payment_order`
- `provider_payment_reference`
- `failure_reason` if failed

### 15.23 `GET /api/v1/bookings/{booking_id}/invoice`

Purpose:

- fetch final invoice for completed booking

Response includes:

- invoice ID
- fare breakdown
- tax breakdown
- payment summary
- refund summary if applicable

### 15.24 `POST /api/v1/bookings/{booking_id}/ratings`

Purpose:

- capture post-trip feedback

Idempotency:

- required

Request Fields:

- `rating` integer `1` to `5`
- `tags` optional string array
- `comment` optional string

Response:

- submitted rating summary

### 15.25 `POST /api/v1/support/tickets`

Purpose:

- create a customer support or incident ticket

Idempotency:

- required

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `category` | string | Yes | issue category |
| `sub_category` | string | No | issue subtype |
| `severity` | string | Yes | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `description` | string | Yes | customer-entered issue |
| `booking_id` | string | No | auto-bound when created in ride context |
| `trip_id` | string | No | auto-bound when created in ride context |
| `media_asset_ids` | array | No | uploaded support attachments |

Response `201`:

- `support_ticket`

### 15.26 `GET /api/v1/support/tickets`

Query Parameters:

- `state`
- `from`
- `to`
- `limit`
- `cursor`

Response:

- list of own `support_ticket`

### 15.27 `GET /api/v1/support/tickets/{ticket_id}`

Purpose:

- fetch full detail for a customer-owned ticket

Response includes:

- ticket detail
- timeline
- linked booking and trip summary
- resolution summary when closed

## 16. Driver APIs

### 16.1 Driver API Domain Summary

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/drivers/me` | get driver profile |
| `PATCH` | `/api/v1/drivers/me` | update editable driver fields |
| `GET` | `/api/v1/drivers/me/dashboard` | get driver home payload |
| `PUT` | `/api/v1/drivers/onboarding` | create or update onboarding submission |
| `GET` | `/api/v1/drivers/onboarding/status` | get onboarding status |
| `POST` | `/api/v1/drivers/documents` | register uploaded driver document |
| `GET` | `/api/v1/drivers/documents` | list uploaded documents |
| `PATCH` | `/api/v1/drivers/me/availability` | toggle online or offline |
| `GET` | `/api/v1/drivers/jobs/upcoming` | list current and upcoming jobs |
| `GET` | `/api/v1/drivers/assignments/offers` | list active assignment offers |
| `GET` | `/api/v1/drivers/assignments/{assignment_id}` | get assignment detail |
| `POST` | `/api/v1/drivers/assignments/{assignment_id}/accept` | accept assignment |
| `POST` | `/api/v1/drivers/assignments/{assignment_id}/decline` | decline assignment |
| `POST` | `/api/v1/drivers/trips/{trip_id}/arrived` | mark arrival |
| `POST` | `/api/v1/drivers/trips/{trip_id}/location-pings` | send live location |
| `GET` | `/api/v1/drivers/trips/{trip_id}` | get trip detail |
| `POST` | `/api/v1/drivers/trips/{trip_id}/complete` | complete trip |
| `GET` | `/api/v1/drivers/earnings/summary` | get earnings summary |
| `GET` | `/api/v1/drivers/earnings/ledger` | get earning ledger |
| `POST` | `/api/v1/drivers/support/tickets` | create driver support ticket |

All driver endpoints require:

- `Authorization: Bearer <driver access token>`
- actor type `DRIVER`

### 16.2 `GET /api/v1/drivers/me`

Response includes:

- driver profile
- onboarding state
- approval state
- compliance summary
- city and zone scope

### 16.3 `PATCH /api/v1/drivers/me`

Editable Fields:

- `full_name`
- `email`
- `languages`
- `emergency_contact`

Non-Editable via this endpoint:

- compliance state
- bank verification state
- approval state

### 16.4 `GET /api/v1/drivers/me/dashboard`

Purpose:

- load driver home screen in one request

Response includes:

- profile summary
- current availability
- active assignment if any
- upcoming jobs
- today's earnings
- active incentive summary
- onboarding blockers if not approved

### 16.5 `PUT /api/v1/drivers/onboarding`

Purpose:

- submit or update driver onboarding data

Request Fields:

- `full_name`
- `date_of_birth`
- `city_id`
- `languages`
- `license_number`
- `aadhaar_number_masked_or_tokenized`
- `pan_number_masked_or_tokenized`
- `bank_account` object
- `emergency_contact` object

Rules:

- submission allowed before approval
- approved drivers may be forced back to correction state only through admin action

Response:

- onboarding status payload

### 16.6 `GET /api/v1/drivers/onboarding/status`

Response includes:

- `driver_onboarding_state`
- missing fields
- missing documents
- reviewer notes
- eligibility to receive assignments

### 16.7 `POST /api/v1/drivers/documents`

Purpose:

- register a completed uploaded document for review

Request Fields:

- `document_type`
- `media_asset_id`
- `document_number` optional when applicable
- `expires_on` optional when applicable

Response `201`:

- document metadata
- verification state

### 16.8 `GET /api/v1/drivers/documents`

Response:

- list of uploaded documents and their verification states

### 16.9 `PATCH /api/v1/drivers/me/availability`

Purpose:

- toggle driver availability

Request Fields:

- `status`: `ONLINE` or `OFFLINE`
- `reason_code` optional for going offline

Rules:

- driver cannot go fully offline if an active assignment or in-progress trip requires completion

Response:

- updated availability state

Failure Cases:

- `AVAILABILITY_CHANGE_NOT_ALLOWED`
- `DRIVER_NOT_APPROVED`

### 16.10 `GET /api/v1/drivers/jobs/upcoming`

Query Parameters:

- `state`
- `from`
- `to`
- `limit`
- `cursor`

Response includes:

- active accepted assignment
- future scheduled jobs
- recent completed jobs when requested

### 16.11 `GET /api/v1/drivers/assignments/offers`

Purpose:

- list currently actionable offers

Response:

- list of offer summaries with expiry and earning preview

### 16.12 `GET /api/v1/drivers/assignments/{assignment_id}`

Response includes:

- booking summary
- pickup and drop snapshots
- earning preview
- current assignment state
- customer masked contact if allowed
- special instructions

### 16.13 `POST /api/v1/drivers/assignments/{assignment_id}/accept`

Purpose:

- accept an offered assignment

Idempotency:

- required

Request Fields:

- `device_location` optional but recommended
- `offer_version` optional for stale-offer protection

Response `200`:

- locked assignment detail
- trip or arrival instructions

Failure Cases:

- `ASSIGNMENT_OFFER_EXPIRED`
- `ASSIGNMENT_ALREADY_ACCEPTED`
- `DRIVER_NOT_APPROVED`

### 16.14 `POST /api/v1/drivers/assignments/{assignment_id}/decline`

Purpose:

- decline an offered assignment

Idempotency:

- required

Request Fields:

- `reason_code`
- `reason_note` optional

Response:

- updated assignment state

### 16.15 `POST /api/v1/drivers/trips/{trip_id}/arrived`

Purpose:

- mark arrival at pickup and trigger customer start-confirmation flow

Idempotency:

- recommended

Request Fields:

- `current_location` optional but recommended
- `arrival_note` optional

Response:

- updated trip and booking state

Rules:

- arrival does not start billing
- customer confirmation remains required

### 16.16 `POST /api/v1/drivers/trips/{trip_id}/location-pings`

Purpose:

- ingest near-real-time location updates for active assignment or trip

Request Fields:

| Field | Type | Required | Notes |
|---|---|---|---|
| `points` | array | Yes | one or more points |

Point Fields:

- `latitude`
- `longitude`
- `captured_at`
- `accuracy_meters` optional
- `speed_kmph` optional
- `heading_degrees` optional

Rules:

- batch size max `10`
- pings older than configured stale threshold may be rejected

Response:

- accepted point count
- last processed timestamp

### 16.17 `GET /api/v1/drivers/trips/{trip_id}`

Response includes:

- trip summary
- booking summary
- customer masked contact
- current fare snapshot
- payout preview or locked payout

### 16.18 `POST /api/v1/drivers/trips/{trip_id}/complete`

Purpose:

- mark trip complete and trigger final fare and payout finalization

Idempotency:

- required

Request Fields:

- `completion_note` optional
- `current_location` optional
- `supporting_media_asset_ids` optional

Response:

- completed trip summary
- final customer fare snapshot
- final driver payout snapshot

Failure Cases:

- `TRIP_ALREADY_COMPLETED`
- `STATE_CONFLICT`

### 16.19 `GET /api/v1/drivers/earnings/summary`

Query Parameters:

- `period`: `TODAY`, `THIS_WEEK`, `THIS_MONTH`, custom date range
- `from`
- `to`

Response includes:

- total completed earnings
- pending settlement
- completed jobs count
- incentive totals
- deductions if any

### 16.20 `GET /api/v1/drivers/earnings/ledger`

Response includes:

- earning ledger rows
- payout component breakdown
- settlement references

### 16.21 `POST /api/v1/drivers/support/tickets`

Purpose:

- allow drivers to report onboarding, trip, payout, or app issues

Idempotency:

- required

Request Fields:

- `category`
- `sub_category` optional
- `severity`
- `description`
- `assignment_id` optional
- `trip_id` optional
- `media_asset_ids` optional

Response:

- `support_ticket`

## 17. Admin and Ops APIs

### 17.1 Admin API Domain Summary

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/admin/auth/login` | admin login |
| `POST` | `/api/v1/admin/auth/refresh` | refresh admin session |
| `POST` | `/api/v1/admin/auth/logout` | logout admin session |
| `GET` | `/api/v1/admin/dashboard/summary` | ops dashboard summary |
| `GET` | `/api/v1/admin/bookings` | list bookings |
| `GET` | `/api/v1/admin/bookings/{booking_id}` | booking deep detail |
| `GET` | `/api/v1/admin/bookings/rescue-queue` | at-risk queue |
| `GET` | `/api/v1/admin/bookings/{booking_id}/assignment-candidates` | candidate drivers for manual rescue |
| `POST` | `/api/v1/admin/bookings/{booking_id}/reassign` | manual reassign |
| `POST` | `/api/v1/admin/bookings/{booking_id}/cancel` | admin cancellation |
| `GET` | `/api/v1/admin/drivers/onboarding-queue` | drivers pending review |
| `GET` | `/api/v1/admin/drivers/{driver_id}` | driver detail |
| `POST` | `/api/v1/admin/drivers/{driver_id}/approve` | approve driver |
| `POST` | `/api/v1/admin/drivers/{driver_id}/reject` | reject driver |
| `POST` | `/api/v1/admin/drivers/{driver_id}/request-correction` | request corrected onboarding |
| `POST` | `/api/v1/admin/drivers/{driver_id}/suspend` | suspend driver |
| `GET` | `/api/v1/admin/support/tickets` | support queue |
| `GET` | `/api/v1/admin/support/tickets/{ticket_id}` | support detail |
| `POST` | `/api/v1/admin/support/tickets/{ticket_id}/actions` | support action |
| `GET` | `/api/v1/admin/incidents` | incident list |
| `GET` | `/api/v1/admin/incidents/{incident_id}` | incident detail |
| `POST` | `/api/v1/admin/incidents/{incident_id}/actions` | incident action |
| `POST` | `/api/v1/admin/refunds` | create refund |
| `GET` | `/api/v1/admin/refunds` | list refunds |
| `GET` | `/api/v1/admin/refunds/{refund_id}` | refund detail |
| `GET` | `/api/v1/admin/pricing/plans` | list pricing plans |
| `POST` | `/api/v1/admin/pricing/plans` | create draft pricing plan |
| `GET` | `/api/v1/admin/pricing/plans/{plan_id}` | get pricing plan |
| `POST` | `/api/v1/admin/pricing/plans/{plan_id}/publish` | publish plan |
| `GET` | `/api/v1/admin/payout/plans` | list payout plans |
| `POST` | `/api/v1/admin/payout/plans` | create draft payout plan |
| `POST` | `/api/v1/admin/payout/plans/{plan_id}/publish` | publish payout plan |
| `GET` | `/api/v1/admin/serviceability/zones` | list service zones |
| `PUT` | `/api/v1/admin/serviceability/zones/{zone_id}` | update zone status |
| `GET` | `/api/v1/admin/audit/logs` | audit search |
| `GET` | `/api/v1/admin/reports/operations` | operational reporting |

All admin endpoints require:

- `Authorization: Bearer <admin access token>`
- actor type `ADMIN`
- route-permission match to bound admin role

### 17.2 `POST /api/v1/admin/auth/login`

Request Fields:

- `email`
- `password`
- `device_id` optional

Response `200`:

- access token
- refresh token
- admin identity
- role list
- city or zone scope if restricted

### 17.3 `POST /api/v1/admin/auth/refresh`

Same semantics as shared refresh endpoint but limited to admin sessions.

### 17.4 `POST /api/v1/admin/auth/logout`

Returns:

- `204`

### 17.5 `GET /api/v1/admin/dashboard/summary`

Purpose:

- load dashboard counters and KPIs for the ops home page

Query Parameters:

- `city_id` optional
- `zone_id` optional

Response includes:

- bookings by state
- at-risk count
- unassigned count
- average assignment time
- support ticket open count
- refund count
- driver onboarding pending count

### 17.6 `GET /api/v1/admin/bookings`

Query Parameters:

- `state`
- `service_type`
- `city_id`
- `zone_id`
- `from`
- `to`
- `q` search by booking number, phone, or driver
- `limit`
- `cursor`

Response:

- list of operational booking summaries

### 17.7 `GET /api/v1/admin/bookings/{booking_id}`

Purpose:

- provide the complete operational truth for one booking

Response includes:

- booking detail
- quote snapshot
- fare snapshot
- assignment history
- trip history
- support links
- payment summary
- refund summary
- audit references
- `row_version`

### 17.8 `GET /api/v1/admin/bookings/rescue-queue`

Purpose:

- show bookings requiring intervention

Query Parameters:

- `priority`
- `city_id`
- `zone_id`
- `reason_code`
- `limit`
- `cursor`

Response includes:

- at-risk bookings with current risk reason
- time to SLA breach
- current assignment status
- suggested rescue action metadata

### 17.9 `GET /api/v1/admin/bookings/{booking_id}/assignment-candidates`

Purpose:

- return candidate drivers for manual assignment or reassignment

Query Parameters:

- `limit`
- `include_ineligible` default `false`

Response includes:

- candidate driver list
- eligibility flags
- distance to pickup
- expected arrival time
- trust and compliance summary
- payout preview summary

### 17.10 `POST /api/v1/admin/bookings/{booking_id}/reassign`

Purpose:

- manually reassign a booking to a selected driver

Idempotency:

- required

Request Fields:

| Field | Type | Required | Notes |
|---|---|---|---|
| `target_driver_id` | string | Yes | selected driver |
| `reason_code` | string | Yes | rescue reason |
| `reason_note` | string | No | optional explanation |
| `row_version` | integer | Yes | optimistic lock |
| `override_acknowledged` | boolean | Yes | actor confirms manual override |

Response `200`:

- updated booking detail
- new assignment summary
- audit reference

Failure Cases:

- `STALE_ROW_VERSION`
- `FORBIDDEN`
- `STATE_CONFLICT`

### 17.11 `POST /api/v1/admin/bookings/{booking_id}/cancel`

Purpose:

- cancel a booking from admin/ops workflow when policy allows

Idempotency:

- required

Request Fields:

- `reason_code`
- `reason_note`
- `refund_recommendation` optional
- `row_version`

Response:

- updated booking state
- refund recommendation summary

### 17.12 `GET /api/v1/admin/drivers/onboarding-queue`

Query Parameters:

- `state`
- `city_id`
- `document_status`
- `limit`
- `cursor`

Response:

- list of driver onboarding records pending review

### 17.13 `GET /api/v1/admin/drivers/{driver_id}`

Response includes:

- driver profile
- onboarding submission
- document checklist
- review notes
- approval history
- compliance status
- operational summary

### 17.14 `POST /api/v1/admin/drivers/{driver_id}/approve`

Purpose:

- approve an onboarding-complete driver for live assignment eligibility

Idempotency:

- required

Request Fields:

- `reason_note` optional
- `row_version`

Response:

- updated onboarding state
- eligibility state
- audit reference

### 17.15 `POST /api/v1/admin/drivers/{driver_id}/reject`

Request Fields:

- `reason_code`
- `reason_note`
- `row_version`

Response:

- updated onboarding state

### 17.16 `POST /api/v1/admin/drivers/{driver_id}/request-correction`

Purpose:

- return onboarding to correction state with explicit notes

Request Fields:

- `required_corrections` array
- `reason_note`
- `row_version`

Response:

- updated onboarding state
- reviewer notes

### 17.17 `POST /api/v1/admin/drivers/{driver_id}/suspend`

Purpose:

- suspend a driver from receiving new assignments

Request Fields:

- `reason_code`
- `effective_until` optional
- `reason_note`
- `row_version`

Response:

- compliance and operational status summary

### 17.18 `GET /api/v1/admin/support/tickets`

Query Parameters:

- `state`
- `category`
- `severity`
- `owner_admin_id`
- `linked_booking_id`
- `limit`
- `cursor`

Response:

- list of support ticket summaries

### 17.19 `GET /api/v1/admin/support/tickets/{ticket_id}`

Response includes:

- ticket detail
- internal notes
- linked resources
- customer and driver context
- current SLA state

### 17.20 `POST /api/v1/admin/support/tickets/{ticket_id}/actions`

Purpose:

- record an auditable support workflow action

Idempotency:

- required

Supported `action_type` values:

- `ASSIGN_OWNER`
- `ADD_INTERNAL_NOTE`
- `CHANGE_SEVERITY`
- `ESCALATE_INCIDENT`
- `REQUEST_REFUND_REVIEW`
- `RESOLVE`
- `REOPEN`

Request Fields:

- `action_type`
- `payload`
- `row_version`

Response:

- updated ticket detail

### 17.21 `GET /api/v1/admin/incidents`

Query Parameters:

- `state`
- `severity`
- `from`
- `to`
- `limit`
- `cursor`

Response:

- list of incident summaries

### 17.22 `GET /api/v1/admin/incidents/{incident_id}`

Response includes:

- incident detail
- linked trip and booking
- escalation timeline
- attached evidence
- current owner

### 17.23 `POST /api/v1/admin/incidents/{incident_id}/actions`

Supported `action_type` values:

- `ASSIGN_OWNER`
- `ADD_NOTE`
- `MARK_CONTACTED`
- `ESCALATE`
- `CLOSE`
- `REOPEN`

Request Fields:

- `action_type`
- `payload`
- `row_version`

Response:

- updated incident detail

### 17.24 `POST /api/v1/admin/refunds`

Purpose:

- create a refund request and trigger provider-side processing where applicable

Idempotency:

- required

Request:

| Field | Type | Required | Notes |
|---|---|---|---|
| `booking_id` | string | Yes | booking being refunded |
| `payment_id` | string | Yes | captured payment reference |
| `amount_paise` | integer | Yes | capped by policy and role |
| `reason_code` | string | Yes | refund reason |
| `reason_note` | string | No | optional explanation |
| `row_version` | integer | Yes | optimistic lock |

Response `201`:

- refund detail
- audit reference

Failure Cases:

- `REFUND_NOT_ALLOWED`
- `OVERRIDE_LIMIT_EXCEEDED`
- `STALE_ROW_VERSION`

### 17.25 `GET /api/v1/admin/refunds`

Query Parameters:

- `state`
- `reason_code`
- `from`
- `to`
- `booking_id`
- `limit`
- `cursor`

Response:

- refund summaries

### 17.26 `GET /api/v1/admin/refunds/{refund_id}`

Response includes:

- full refund workflow
- payment linkage
- actor notes
- provider transaction reference

### 17.27 `GET /api/v1/admin/pricing/plans`

Purpose:

- list pricing plans and publication state

Query Parameters:

- `city_id`
- `status`
- `service_type`
- `limit`
- `cursor`

### 17.28 `POST /api/v1/admin/pricing/plans`

Purpose:

- create a draft pricing plan

Request Fields:

- `city_id`
- `name`
- `effective_from`
- `rules` array
- `tax_profile_version`

Response:

- draft pricing plan

### 17.29 `GET /api/v1/admin/pricing/plans/{plan_id}`

Response includes:

- plan metadata
- full rule set
- publication history

### 17.30 `POST /api/v1/admin/pricing/plans/{plan_id}/publish`

Purpose:

- publish a draft pricing plan for future quotes

Idempotency:

- required

Request Fields:

- `effective_from`
- `reason_note`

Response:

- plan state and publication timestamp

### 17.31 `GET /api/v1/admin/payout/plans`

Purpose:

- list payout plans and publication state

### 17.32 `POST /api/v1/admin/payout/plans`

Purpose:

- create a draft payout plan

### 17.33 `POST /api/v1/admin/payout/plans/{plan_id}/publish`

Purpose:

- publish a payout plan for future accepted assignments

### 17.34 `GET /api/v1/admin/serviceability/zones`

Purpose:

- list service zones, active status, and current operational settings

Query Parameters:

- `city_id`
- `status`

### 17.35 `PUT /api/v1/admin/serviceability/zones/{zone_id}`

Purpose:

- change zone serviceability status or metadata

Request Fields:

- `status`
- `service_types_enabled`
- `reason_note`
- `row_version`

Response:

- updated zone detail

### 17.36 `GET /api/v1/admin/audit/logs`

Purpose:

- search auditable actions

Query Parameters:

- `entity_type`
- `entity_id`
- `action_type`
- `actor_user_id`
- `from`
- `to`
- `limit`
- `cursor`

Response:

- audit log summaries with actor, timestamp, action, and reason

### 17.37 `GET /api/v1/admin/reports/operations`

Purpose:

- retrieve aggregate operational KPIs for dashboards and exports

Query Parameters:

- `city_id`
- `zone_id`
- `from`
- `to`
- `granularity`

Response includes:

- bookings created
- fulfillment rate
- assignment time
- cancellation rate
- refund rate
- support volume
- gross revenue and payout summary

## 18. Webhooks and External Callback APIs

### 18.1 `POST /api/v1/webhooks/payments/{provider}`

Purpose:

- ingest payment provider callbacks for payment success, failure, refund, and reconciliation events

Auth:

- provider signature verification required

Requirements:

- raw request body must be preserved for signature verification
- callback processing must be idempotent
- event receipt must be stored in an append-only webhook event log

Supported Event Outcomes:

- payment captured
- payment failed
- refund processed
- refund failed

Response:

- `200` once accepted and recorded

Failure behavior:

- invalid signatures return `401`
- valid but duplicate events return `200` with no duplicate side effect

## 19. Security, Audit, and Data Protection Requirements

### 19.1 PII Handling

- customer and driver phone numbers must be masked when exposed cross-actor
- full KYC details must never be returned to customer endpoints
- admin responses should be field-scoped to role where appropriate

### 19.2 Audit Requirements

The following actions must create audit records:

- booking cancellation after confirmation
- booking modification after confirmation
- manual reassignment
- admin cancellation
- refund creation or reversal
- driver approval, rejection, suspension, correction request
- pricing or payout plan publish
- serviceability changes

### 19.3 Signature and Callback Security

- payment webhooks must validate provider signature
- replay protection is mandatory
- webhook events must be traceable to booking and payment records

### 19.4 Device and Session Controls

- session refresh rotation is mandatory
- server must be able to revoke sessions by user or device
- excessive failed OTP or login attempts must trigger throttling

## 20. Initial Rate-Limit Baseline

The exact values may be tuned operationally, but the contract should assume rate limiting exists.

Recommended defaults:

- OTP request: `3 per 15 minutes per mobile number`
- OTP verify: `5 attempts per challenge`
- quote creation: `30 per 15 minutes per customer`
- booking creation: `10 per 15 minutes per customer`
- location pings: `1 request every 5 seconds per driver device`
- admin login: `10 per 15 minutes per account or IP`

## 21. API Release Acceptance Criteria

The MVP API layer should not be treated as ready until all of the following are true:

- request and response schemas are implemented exactly or revised in this document first
- all critical write endpoints support idempotency
- all financial and admin override routes are audited
- customer booking flow works end to end from OTP to payment
- driver flow works end to end from onboarding to payout summary
- admin rescue, refund, and onboarding approval flows work with RBAC
- webhook processing is signature-verified and idempotent
- error contract is consistent across modules

## 22. Recommended Next Step

The next implementation document after this API contract should be a `Low_Level_Design.md` for the Spring Boot modules and application services, using this file as the external interface source of truth.

# Rydvrse API Integration Mapping (Swagger -> UI)

## 1. Purpose

Map major UI actions to backend APIs so product, frontend, backend, and QA can validate full flow consistency.

---

## 2. Customer App Mapping

| UI Action | Screen | API | Notes |
|---|---|---|---|
| Request OTP | Customer login | `POST /auth/otp/request` | Role = CUSTOMER |
| Verify OTP | Customer OTP | `POST /auth/otp/verify` | Session tokens hydrated in Redux |
| Save profile | Profile setup/edit | `PATCH /customers/me` | Supports basic profile fields |
| Fetch home context | Home boot | `GET /customers/me/home` | Optional for upcoming trips |
| Search place | Location picker | Ola Places API | Autocomplete + fallback list |
| Reverse geocode current location | Home/picker | Ola Places API | Used to label GPS pickup |
| Create quote | Quote flow | `POST /quotes` | Uses selected pickup/drop coordinates |
| Create booking | Review confirm | `POST /bookings` | Quote id + trip metadata |
| List bookings | Bookings tab | `GET /bookings` | Includes status and fare snapshot |
| Booking detail | Booking detail | `GET /bookings/{id}` | Full booking context |
| Confirm trip start | Start trip | `POST /bookings/{id}/start-confirmation` | OTP-gated flow |
| Create payment order | Payment screen | `POST /bookings/{id}/payment-orders` | Handles payment retry path |
| Fetch invoice | Payment/invoice | `GET /bookings/{id}/invoice` | Line-item billing |
| Submit rating/issue | Post-payment | `POST /bookings/{id}/ratings` | Optional issue capture |
| Create support ticket | Support/chat escalation | `POST /support/tickets` | Context-linked support |

---

## 3. Driver App Mapping

| UI Action | Screen | API | Notes |
|---|---|---|---|
| Request OTP | Driver login | `POST /auth/otp/request` | Role = DRIVER |
| Verify OTP | Driver OTP | `POST /auth/otp/verify` | Session tokens hydrated in Redux |
| Submit onboarding data | Document upload | `POST /drivers/onboarding` | Status becomes UNDER_REVIEW |
| Read dashboard | Driver home | `GET /drivers/me/dashboard` | Earnings + availability |
| Toggle availability | Driver home | `PATCH /drivers/me/availability` | AVAILABLE/UNAVAILABLE |
| Fetch offers | Driver home/jobs | `GET /drivers/me/offers` | Assignment previews |
| Accept offer | Offer detail | `POST /drivers/offers/{id}/accept` | Binds active assignment |
| Mark arrived | Pickup | `POST /drivers/trips/{id}/arrive` | Enables OTP wait state |
| Complete trip | Trip complete | `POST /drivers/trips/{id}/complete` | Triggers payout ledger |
| Earnings ledger | Earnings tab | `GET /drivers/me/earnings-ledger` | Payout transparency |

---

## 4. Booking + Pricing Critical Mapping

## 4.1 Quote Payload Integration Fields

Frontend must send:

- `service_type`
- `pickup.label`, `pickup.latitude`, `pickup.longitude`
- `drop.label`, `drop.latitude`, `drop.longitude`
- `scheduled_pickup_at`
- `rounded_distance_km`
- `predicted_drive_minutes`
- `driver_pickup_distance_km`
- `driver_pickup_eta_minutes`
- `transmission_type`
- `car_type`
- `car_brand_model`
- `car_number`

## 4.2 Runtime Notes

- UI now stores pickup/drop coordinates in Redux booking form.
- Quote no longer relies on static coordinate constants.
- If user-selected coordinates are missing, frontend infers Bengaluru coordinates from label as a fallback.

---

## 5. Error Contract Expectations

Frontend should support these backend response classes:

- `400/422`: invalid request or validation issue
- `401/403`: auth/session issues
- `404`: missing booking/offer
- `409`: stale quote or state conflict
- `5xx`: retryable server failures

UI behavior:

- show concise human-readable message
- retain user-entered form values
- expose a clear retry action

---

## 6. Testing Matrix (Integration)

1. Customer end-to-end:
   - location select -> quote -> booking -> trip start -> payment
2. Driver end-to-end:
   - onboarding (mock) -> offer -> pickup -> start -> complete
3. Support path:
   - customer and driver chat escalation with contextual trip reference
4. Failure path:
   - quote failure + retry
   - GPS denied + manual selection
   - map provider unavailable fallback

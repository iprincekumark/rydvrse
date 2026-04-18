# Rydvrse System Architecture (Frontend + Backend)

## 1. Purpose

Define the production-oriented integration architecture between:

- `rydvrse-mobile` (React Native / Expo)
- `rydvrse-server` (Spring Boot modular backend)

This document focuses on runtime flow integrity for booking, routing, pricing, support, and trip lifecycle.

---

## 2. Frontend Architecture (Mobile)

## 2.1 Layering

- `screens/` -> orchestration and screen-level state transitions
- `components/` -> reusable UI primitives + composed sections
- `services/api/` -> HTTP integration
- `services/maps/` -> autocomplete, reverse geocode, route estimate helpers
- `store/` -> Redux source of truth for session, booking form, quote, offers
- `hooks/` -> cross-screen utilities (`useCurrentLocation`)

## 2.2 Core Runtime Flows

1. Session bootstrap (`sessionSlice`)
2. Booking form hydration (`customerSlice`)
3. Live map and GPS updates (`useCurrentLocation`)
4. Route estimate update (`routeEstimator`)
5. Quote creation (`customerApi.quote`)
6. Booking creation (`customerApi.createBooking`)

## 2.3 Reliability Rules

- Native map module optional at runtime with safe fallback map UI
- GPS optional with Bengaluru fallback coordinate
- Mocks supported for local UI testing when backend is unavailable

---

## 3. Backend Architecture (Spring Boot)

## 3.1 Domain Modules (high-level)

- `auth`
- `customer`
- `driver`
- `booking`
- `pricing`
- `dispatch`
- `trip`
- `tracking`
- `finance`
- `support`
- `admin/reporting`

## 3.2 Commercial Core

- Quote generation uses Bengaluru hybrid pricing logic
- Quote snapshot is used at booking confirmation
- Assignment and payout previews are traceable from quote context

## 3.3 Persistence and Infra

- PostgreSQL + Flyway migrations
- Redis for cache/hot state/queue-like workflows where configured
- Observability via logs + actuator endpoints

---

## 4. Frontend <-> Backend Contract Boundaries

## 4.1 Strong Contracts

- `POST /api/v1/quotes` must receive:
  - service type
  - pickup/drop labels
  - pickup/drop coordinates
  - rounded distance and predicted drive minutes
  - vehicle metadata (transmission, car type, brand/model, number)

- `POST /api/v1/bookings` must receive:
  - quote id
  - service type
  - pickup/drop labels
  - schedule
  - optional notes

## 4.2 Validation and Fallback Rules

- Frontend should always send selected coordinates where available.
- If coordinates are unavailable, frontend may send inferred Bengaluru coordinates to preserve booking flow continuity.
- Backend remains final authority on fare calculation and validation.

---

## 5. Real-Time and Tracking Flow

1. Device provides foreground location updates.
2. Customer/driver home screens render live position on map.
3. Pickup/drop markers remain tied to selected trip data.
4. ETA and route summaries are derived from map services + backend pricing estimates.

---

## 6. Failure Handling Strategy

- Map SDK unavailable -> fallback preview map + preserved booking flow
- GPS denied -> manual search path remains enabled
- Places API timeout -> fallback Bengaluru suggestions
- Quote API failure -> recoverable error with retry action

---

## 7. Security and Secret Handling

- Mobile runtime keys are passed using Expo public env variables.
- Actual secrets must stay in local `.env.local` / CI secrets, not git history.
- `.env.example` provides placeholder keys and URLs.

---

## 8. Deployment Readiness Notes

- Mobile:
  - `npm install`
  - `npm run typecheck`
  - `CI=1 npm test -- --watchman=false --runInBand`
  - `npx expo start --lan` or `--localhost`

- Backend:
  - run Postgres/Redis containers
  - run Spring Boot service
  - verify actuator and core APIs

This architecture enables incremental UX evolution without destabilizing pricing/booking backend behavior.

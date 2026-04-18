# Rydvrse Feature-wise Explanation

## 1. Core Product Feature Set

Rydvrse MVP is intentionally focused on a narrow, reliable core:

1. `Book Driver` (customer)
2. `Fulfill Job` (driver)
3. `Support & Recovery` (customer + driver + ops)

---

## 2. Customer Features

## 2.1 Authentication

- OTP login and verification
- Session persisted in Redux state

## 2.2 Home + Booking

- Map-first home
- One-way / round-trip selector
- Pickup and drop selection
- Quick schedule controls
- Route summary and fare entry CTA

## 2.3 Car Details + Fare

- Transmission type
- Car type
- Brand/model
- Car number
- Optional notes

Quote output:

- itemized fare components
- cancellation summary
- driver payout transparency signal

## 2.4 Booking Lifecycle

- Confirm booking from quote
- Track assignment status
- View assigned driver + OTP
- Confirm trip start
- Track active trip
- Complete payment, rating, and issue reporting

## 2.5 Support and Profile

- FAQ search
- Chat entry point
- Booking-linked support path
- Editable profile fields

---

## 3. Driver Features

## 3.1 Authentication and Onboarding

- OTP login
- Checklist-based onboarding
- Document submission
- Review status handling

## 3.2 Availability and Offer Intake

- Toggle availability
- View incoming jobs
- Accept assignment with earning preview

## 3.3 Trip Operations

- Navigate to pickup
- Mark arrival
- Verify customer OTP
- Start and complete trip

## 3.4 Earnings and Support

- Ledger-style payout view
- FAQ + support chat
- Profile with operational status chips

---

## 4. Shared UX and System Features

## 4.1 Compact Interface Design

- Reduced copy length
- Lower scroll pressure on help/profile
- Fast action-first cards

## 4.2 Real-time Map Strategy

- Interactive map where module available
- Live GPS updates from device
- Pickup/drop route context
- Styled fallback map when native map runtime is unavailable

## 4.3 Error and Resilience

- GPS denied fallback
- map/provider fallback
- skeleton/loading states
- empty states with clear actions

---

## 5. Pricing and Commercial Features

- Quote-before-book model
- Bengaluru hybrid input model (distance + time + pickup effort)
- one-way and round-trip support
- transparent fare composition
- backend-driven final pricing authority

---

## 6. Operational and Scalability Features

- Support ticket hooks
- Booking and payout traceability
- State-based lifecycle transitions
- API-first modular expansion path for dispatch/tracking/reporting

---

## 7. Feature Completion Priorities

## 7.1 Completed in current iteration

- coordinate-aware quote creation
- compact customer and driver help/profile refinements
- map interactivity path with fallback
- live location watch integration

## 7.2 Next upgrades

1. Persist recent/saved places per account.
2. Add full route polyline from directions response.
3. Add richer incident states (reassignment, delay, payment-failed overlays).
4. Extend driver profile edit flow to backend persistence.

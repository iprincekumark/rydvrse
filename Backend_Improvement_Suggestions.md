# Backend Improvement Suggestions for UI/UX Reliability

## 1. Purpose

Capture backend enhancements that improve customer and driver UX quality without changing the core business direction.

---

## 2. High-Impact Improvements

## 2.1 Quote Quality Metadata Expansion

Add fields in quote response:

- `estimate_quality` (`LIVE_ROUTE`, `FALLBACK_ROUTE`)
- `coordinate_confidence` (`HIGH`, `MEDIUM`, `LOW`)
- `route_provider_latency_ms`

Why:

- UI can show confidence-sensitive hints only when required.

## 2.2 Route Context Endpoint

Add optional endpoint:

- `POST /api/v1/routes/preview`

Input:

- pickup/drop coordinates + service type

Output:

- encoded route polyline
- distance/time
- provider confidence

Why:

- lets mobile render exact route preview independent from quote mutation.

## 2.3 Support Context Enrichment

Support ticket response should include:

- booking id
- assignment id
- last known trip state
- payment/refund status summary

Why:

- reduces chat back-and-forth and improves first-response resolution.

## 2.4 Profile Edit Parity

Add/confirm driver profile update endpoint parity with customer profile update.

Why:

- completes editable profile requirement across both roles.

---

## 3. Operational and Performance Suggestions

1. Cache place/route lookups with short TTL for hot corridors.
2. Add timeout budgets and fallback flags for map provider calls.
3. Emit structured events for quote failures and coordinate fallbacks.
4. Create alerting for quote-to-booking drop due to pricing errors.

---

## 4. API Safety Recommendations

- enforce idempotency for quote and booking writes
- validate lat/lng ranges server-side
- reject impossible route assumptions (zero distance + high duration mismatch) with explicit error codes

---

## 5. Suggested Rollout Sequence

1. Quote metadata expansion
2. Route preview endpoint
3. Support context enrichment
4. Driver profile edit parity

This order yields fast UX gains with low platform risk.

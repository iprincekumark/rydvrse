# UI Implementation Roadmap

## 1. Objective

Deliver a production-ready, compact, map-first UX without breaking existing app flows.

---

## 2. Phase Plan

## Phase 1 - Stabilize Core Booking UX (Completed)

- Add coordinate-aware booking form state
- Replace hardcoded quote coordinates
- Integrate interactive map with fallback behavior
- Add live GPS watch updates
- Compact booking sheet and route summary

Exit criteria:

- Quote payload always receives pickup/drop coordinates when available
- Home booking flow remains functional in mock + API modes

## Phase 2 - Compactness and Consistency (Completed)

- Shorten verbose copy
- Align customer and driver support/profile structures
- Standardize back/navigation behavior for help/profile roots

Exit criteria:

- Help/profile screens have reduced scroll pressure
- Navigation behavior is consistent

## Phase 3 - Product Hardening (Next)

1. True route polyline decode from map directions response.
2. Saved places and recent search persistence.
3. Driver profile edit API support.
4. Better empty/error state observability.

Exit criteria:

- Full customer + driver critical journeys verified end-to-end.

---

## 3. Validation Gates

- `npm run typecheck`
- `CI=1 npm test -- --watchman=false --runInBand`
- Local Expo run in `--lan` and `--localhost` modes
- Backend quote/booking runtime sanity checks

---

## 4. Rollout Strategy

1. Ship with feature-safe fallback map layer.
2. Monitor quote conversion and support tickets for location/fare issues.
3. Tune pricing assumptions and map confidence thresholds from live telemetry.

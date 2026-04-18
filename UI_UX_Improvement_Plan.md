# Rydvrse UI/UX Improvement Plan

## Document Control

- Product: `Rydvrse`
- Scope: `Customer + Driver mobile app (React Native / Expo)`
- Version: `3.0`
- Date: `2026-04-18`
- Design direction: `Uber-like usability, Rydvrse light-green identity`
- Constraint: `Do not break existing system behavior`

---

## 1. Goal

Upgrade the current working app into a clean, compact, production-ready interface while keeping all existing business flows functional:

- Auth -> booking -> quote -> booking confirm -> trip -> payment -> support
- Driver onboarding -> offers -> pickup -> trip -> payout -> support

Primary booking intent remains intentionally simple:

- `Book Driver`
- `One-way trip`
- `Round trip`

No extra booking products should clutter primary customer surfaces.

---

## 2. Non-Negotiables

1. No static mock-only screens.
2. No hardcoded fare-critical map values in final booking/quote payloads.
3. One pickup and one drop only.
4. Trip type selected once.
5. Compact layout for Home, Help, and Profile (reduced scroll).
6. Back action behavior must be consistent and predictable.
7. All map, pricing, and support states must handle loading/error/fallback cleanly.

---

## 3. Design System Direction

### 3.1 Visual Identity

- Primary color: `#B7F34D`
- Strong color: `#8FD62F`
- Primary text: `#101312`
- Background: `#F7F8F4`
- Surfaces: `#FFFFFF`

### 3.2 Style Rules

- Minimal copy, direct labels
- Small readable typography
- Large enough touch targets
- Card + bottom-sheet rhythm
- Remove redundant icons and duplicate artwork

### 3.3 Interaction Rules

- Main booking action visible above fold
- Search and location actions should be fast, single-purpose
- Primary CTA labels must be explicit: `Get fare`, `Confirm booking`, `Start trip`

---

## 4. Current-to-Target Gap Summary

| Area | Before | Target |
|---|---|---|
| Home map | Stylized static map | Real interactive map with graceful fallback |
| GPS | One-time fetch | Continuous location updates where needed |
| Quote location payload | Hardcoded coords in quote call | Selected pickup/drop coords sent to backend |
| Booking sheet | Extra current-location action + long flow | Compact sheet, one-pass flow |
| Help/Profile | Verbose, more scroll | Compact, one-screen-first structure |
| Customer/Driver consistency | Similar but uneven | Shared UX behavior and compact patterns |

---

## 5. Customer UX Blueprint

## 5.1 Home (Post Login)

Structure:

- Top half: interactive map
- Bottom half: compact booking sheet

Booking sheet sections:

1. Trip type selector (`One-way`, `Round trip`)
2. Pickup row
3. Drop row
4. Quick schedule chips (`Now`, `30 min`, `1 hour`)
5. Pickup time + route summary
6. `Get fare` CTA

Rules:

- Pickup defaults from live GPS (editable)
- Drop selected via search
- No duplicate pickup/drop fields
- No extra detail button on this step

## 5.2 Location Picker

- Single-purpose modal
- Search with autosuggestions
- One `Use current location` action shown only when not actively searching
- Selecting result writes both label and coordinates

## 5.3 Fare + Details Flow

After `Get fare`:

1. Car details screen (transmission, car type, brand/model, car number, notes)
2. Quote screen (itemized fare, cancellation summary, payout transparency)
3. Booking review and confirmation

---

## 6. Driver UX Blueprint

## 6.1 Driver Home

- Availability + earnings snapshot
- Incoming work list
- Interactive map card for spatial context

## 6.2 Pickup and Active Trip

- Interactive route map
- Clear primary action hierarchy:
  - Mark arrived
  - Verify OTP
  - Start trip
  - Complete trip

## 6.3 Driver Help/Profile

- Same compact information architecture as customer
- Reduced explanatory paragraphs
- Focus on immediate actions (chat, earnings issue, logout)

---

## 7. Map and Location Strategy

## 7.1 Runtime Behavior

- Primary path: `react-native-maps` interactive map
- Location source: `expo-location`
- Fallback path: existing stylized map surface if native map module is unavailable

## 7.2 Route Rendering

- Markers for pickup and drop
- Route preview line between markers
- Distance/time chip shown in compact form
- Internal route/fare calculations remain backend-driven and map-service-assisted

## 7.3 Coordinate Integrity

- Location picker stores both label and coordinates in booking form state
- Quote request uses selected coordinates (with inferred Bengaluru fallback only if missing)

---

## 8. Navigation and Compactness Rules

1. Help/Profile screens use compact cards and short labels.
2. Top header/back behavior is consistent.
3. Back actions in tab roots route to Home where appropriate.
4. Avoid long explanatory copy blocks.

---

## 9. State and API Integrity

## 9.1 Redux Booking Form Enhancements

Added fields:

- `pickupLatitude`
- `pickupLongitude`
- `dropLatitude`
- `dropLongitude`

## 9.2 API Contract Usage

Quote payload now derives from user-selected coordinates rather than static defaults.

This aligns the UI with backend pricing logic for:

- rounded distance
- predicted drive time
- driver pickup access costs

---

## 10. Error, Loading, and Edge State Design

Mandatory states:

- GPS denied/unavailable
- Map SDK unavailable fallback
- Search failure fallback suggestions
- Quote loading skeleton
- Empty support search result
- Booking not found / assignment missing

Behavior rule:

- Every state must keep a usable next action visible.

---

## 11. Accessibility and Performance Baseline

Accessibility:

- Semantic button roles
- Readable contrast for text and CTAs
- No critical interaction hidden behind tiny touch zones

Performance:

- Keep map-heavy surfaces compact
- Avoid unnecessary rerenders in booking flow
- Use fallback rendering when optional map modules are unavailable

---

## 12. Incremental Migration Plan (Shipped + Next)

## 12.1 Shipped in this iteration

- Interactive map integration path with fallback
- Live location updates (watch mode)
- Coordinate-aware booking state
- Quote payload coordinate fix
- Compact Help/Profile updates (customer + driver)
- Reduced verbose copy across driver flow headers

## 12.2 Next recommended steps

1. Add route polyline decoding from provider response for exact roads.
2. Add persisted recent/saved places per user.
3. Add driver profile edit flow with API persistence parity.
4. Add visual regression checks for compact screen-height targets.

---

## 13. Acceptance Checklist

- [x] Customer home map + compact booking sheet
- [x] Trip type one-time selection
- [x] One pickup / one drop flow
- [x] Quote uses selected coordinates
- [x] Real-time location updates wired
- [x] Driver and customer support/profile compacted
- [x] Back behavior standardized for support/profile roots
- [x] Typecheck passes
- [x] Unit tests pass (`jest --watchman=false`)

---

## 14. Related Documents

- `System_Architecture_Frontend_Backend.md`
- `API_Integration_Mapping.md`
- `Feature_Wise_Explanation.md`
- `Pricing_Calculation_Documentation.md`

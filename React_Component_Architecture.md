# React Component Architecture (Rydvrse Mobile)

## 1. Purpose

Define maintainable component boundaries for the upgraded UI/UX system.

---

## 2. Layered Structure

```
src/
  assets/
  components/
    primitives/
    common/
    cards/
    layout/
    booking/
    maps/
    feedback/
    loaders/
  hooks/
  screens/
    customer/
    driver/
    shared/
  services/
    api/
    maps/
  store/
  constants/
  theme/
  utils/
```

---

## 3. Responsibility Boundaries

## 3.1 Screens

- compose flows
- call API/services
- interact with Redux
- perform navigation transitions

## 3.2 Components

- reusable visual and interaction building blocks
- no business-side effects
- accept data and callbacks via props

## 3.3 Services

- API requests and normalization
- map provider integration
- route estimate and coordinate helper logic

## 3.4 Store

- session state
- booking form state
- quote and booking state
- driver availability/offers state

---

## 4. Key Component Contracts

## 4.1 `RydvrseMapPreview`

Inputs:

- pickup/drop labels
- optional pickup/drop/current coordinates
- distance and duration display labels
- callbacks for search/recenter
- `interactive` + `minimal` display modes

Behavior:

- uses native map when available
- falls back to safe stylized map

## 4.2 `BookingHomeSheet`

Inputs:

- service type
- pickup/drop labels
- schedule label
- route summary labels
- callbacks for edits and CTA

Behavior:

- one-pass compact booking entry

## 4.3 `ScreenHeader`

Inputs:

- title/subtitle
- optional back handler
- optional right slot

Behavior:

- top-level consistency for stack screens

---

## 5. Recommended Next Refactors

1. Split large customer and driver screen files into feature submodules.
2. Add shared compact `SupportPanel` component used by both roles.
3. Add shared `ProfileActionsList` and `ProfileHeroCard`.
4. Add dedicated map hook for region, route, and marker derivation.

---

## 6. Testing Strategy by Layer

- Components: snapshot-free behavior tests (press/visibility/state)
- Screens: flow tests with Redux + navigation wrappers
- Services: API response normalization tests
- Store: reducer-level unit tests for booking and session updates

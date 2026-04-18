# Rydvrse Pricing Calculation Documentation

## 1. Purpose

Provide a single implementation-ready reference for how fare is calculated for:

- `One-way trip`
- `Round trip`

This document is Bengaluru-focused and optimized for:

- customer affordability
- fair driver earnings
- positive platform economics

---

## 2. Scope and Product Guardrails

For current customer-facing MVP booking flow:

- only `One-way` and `Round trip` are exposed
- pricing is quote-first and transparent
- backend is source of truth for final fare

---

## 3. Input Variables

| Field | Description |
|---|---|
| `rounded_distance_km` | Rounded route distance (e.g., 34.3 -> 35) |
| `predicted_drive_minutes` | Traffic-aware estimated driving minutes |
| `driver_pickup_distance_km` | Estimated distance driver travels to pickup |
| `driver_pickup_eta_minutes` | Pickup ETA estimate |
| `service_type` | `SCHEDULED_ONE_WAY` or `SCHEDULED_ROUND_TRIP` |
| `transmission_type` | `MANUAL` or `AUTOMATIC` |
| `car_type` | `HATCHBACK`, `SEDAN`, `SUV`, `LUXURY` |
| `scheduled_pickup_at` | Needed for peak/night rules |

---

## 4. Fare Formula Overview

```
Total Fare = Taxable Fare + GST

Taxable Fare =
  Base Fare
  + Extra Distance Charge
  + Extra Time Charge
  + Driver Pickup Access
  + Peak Adjustment (if applicable)
  + Night Adjustment (if applicable)
  + Optional Safety Cover
  + Platform Fee
  - Round-trip Bundle Discount (round trip only)
```

---

## 5. One-way Pricing Logic

## 5.1 Baseline Constants (Bengaluru launch)

- Base fare: `₹239`
- Included distance: `20 km`
- Included time: `60 min`
- Extra distance: `₹6.00 / km`
- Extra time: `₹1.80 / min`
- Platform fee: `₹18`
- Safety cover: `₹12` (optional, enabled by default)

## 5.2 Driver Pickup Access

Driver pickup access compensates pre-trip effort:

```
Pickup Access = min(₹55, max(₹18, ₹3.5 * driver_pickup_distance_km))
```

If distance unavailable, fallback:

```
Pickup Access = min(₹55, max(₹18, ₹1.4 * driver_pickup_eta_minutes))
```

## 5.3 One-way Formula

```
extra_distance_km = max(0, rounded_distance_km - 20)
extra_time_min = max(0, predicted_drive_minutes - 60)

taxable_fare =
  239
  + (extra_distance_km * 6.00)
  + (extra_time_min * 1.80)
  + pickup_access
  + peak_adjustment
  + night_adjustment
  + safety_cover
  + 18

final_fare = round(taxable_fare * 1.05)   // 5% GST
```

---

## 6. Round-trip Pricing Logic

## 6.1 Baseline Constants (Bengaluru launch)

- Base fare: `₹399`
- Included distance: `40 km`
- Included driving time: `120 min`
- Included waiting time: `30 min`
- Extra distance: `₹5.50 / km`
- Extra drive time: `₹1.60 / min`
- Platform fee: `₹24`
- Safety cover: `₹12` (optional)
- Bundle discount: `₹70` (fixed launch discount)

## 6.2 Round-trip Formula

```
extra_distance_km = max(0, rounded_distance_km - 40)
extra_drive_time_min = max(0, predicted_drive_minutes - 120)

taxable_fare =
  399
  + (extra_distance_km * 5.50)
  + (extra_drive_time_min * 1.60)
  + pickup_access
  + peak_adjustment
  + night_adjustment
  + safety_cover
  + 24
  - 70

final_fare = round(taxable_fare * 1.05)
```

Round-trip must feel like a discounted bundle, not two one-way fares added.

---

## 7. Peak and Night Logic

## 7.1 Peak Logic (Bengaluru-specific)

Trigger conditions (any one):

- pickup in defined office-peak windows
- predicted route speed below threshold
- demand utilization above threshold

Recommended launch windows:

- Morning: `08:00-11:00`
- Evening: `17:00-21:30`

Adjustment:

```
peak_adjustment = min(₹90, taxable_core * peak_factor)
peak_factor range: 0.08 to 0.18
```

Where `taxable_core` is:

- base + distance + time + pickup_access

## 7.2 Night Logic

If scheduled pickup between `22:00-05:59`:

- one-way night adjustment: `₹60`
- round-trip night adjustment: `₹90`

---

## 8. Driver Payout Model

Driver payout must remain predictable and fair:

```
driver_core = base + distance + time components
driver_share = 0.70 * driver_core

driver_payout =
  driver_share
  + pickup_access
  + peak_bonus
  + night_bonus
```

Bonus rules:

- `peak_bonus`: 35% of peak adjustment
- `night_bonus`: 100% pass-through of night adjustment

This keeps driver incentives aligned with hard-time and late-hour jobs.

---

## 9. Platform Revenue Model

```
platform_net_before_gst =
  taxable_fare
  - driver_payout
  - payment_gateway_cost
```

Guidelines:

- Keep positive margin in non-peak and peak windows
- Watch margin drift when fallback ETA quality is low
- Never hide costs through post-trip surprise charges

---

## 10. Worked Examples

## 10.1 Example A: 31 km One-way (Non-peak, daytime)

Inputs:

- distance: `31 km`
- predicted drive time: `105 min`
- pickup distance: `8 km`
- one-way

Calculation:

- Base: `239`
- Extra distance: `(31-20)*6 = 66`
- Extra time: `(105-60)*1.8 = 81`
- Pickup access: `max(18, 3.5*8) = 28`
- Platform fee: `18`
- Safety: `12`
- Peak: `0`
- Night: `0`

Taxable fare = `239 + 66 + 81 + 28 + 18 + 12 = 444`

Final fare = `444 * 1.05 = 466` (rounded)

### Comparison with Reference Approach 1

Reference approach one-way (same 31 km / 105 min):

- Base 349
- Extra time (45 min * 2.58) = 116
- Extra distance (3 km * 9) = 27
- One-way charge = 116
- Secure fee = 18
- Taxes/fees = 38

Total approx = `₹664` (without night)

Rydvrse example = `₹466`  
Estimated customer saving = `₹198` (~30% lower)

## 10.2 Example B: Equivalent Round-trip

Inputs:

- distance: `62 km`
- predicted drive time: `225 min`
- pickup distance: `8 km`
- round-trip

Calculation:

- Base: `399`
- Extra distance: `(62-40)*5.5 = 121`
- Extra drive time: `(225-120)*1.6 = 168`
- Pickup access: `28`
- Platform fee: `24`
- Safety: `12`
- Bundle discount: `-70`
- Peak/night: `0`

Taxable fare = `399 + 121 + 168 + 28 + 24 + 12 - 70 = 682`

Final fare = `682 * 1.05 = 716` (rounded)

This is significantly cheaper than charging two separate one-way rides.

---

## 11. Edge Cases and Guardrails

1. Zero/invalid distance:
   - use minimum charge envelope and mark quote with `estimate_quality=FALLBACK`
2. Missing ETA:
   - use conservative city speed fallback
3. Missing pickup access input:
   - derive from ETA fallback formula
4. Quote expiry:
   - expired quote cannot be booked; regenerate quote
5. Negative discounts:
   - floor at zero taxable component level where applicable

---

## 12. Admin Override Rules

Allowed override categories:

- goodwill adjustment
- exceptional traffic or map outage correction
- verified driver delay compensation

Rules:

- role-restricted
- mandatory reason code
- mandatory actor identity + timestamp
- full audit trail

---

## 13. Implementation Notes

- Keep formulas configuration-driven in backend.
- Snapshot computed pricing assumptions with each quote and booking.
- Use analytics to tune:
  - included distance/time
  - peak bands
  - pickup access caps
  - bundle discount

This model is startup-friendly, Bengaluru-realistic, and protects all three parties:
customer, driver, and platform.

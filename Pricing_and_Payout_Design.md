# Rydvrse Pricing and Payout Design

## Document Control

- Document Name: `Pricing_and_Payout_Design.md`
- Product: `Rydvrse`
- Version: `1.0`
- Status: `Baseline Commercial Engine Design for MVP`
- Last Updated: `2026-04-10`
- Source Documents:
  - `High_Level_Design.md`
  - `MVP_Scope.md`
  - `Product_Requirements_Document.md`
  - `Screen_Flow.md`

## 1. Purpose

This document defines the pricing and payout engine for the Rydvrse MVP.

It covers:

- fare rules
- driver payout rules
- cancellation charges
- refund rules
- admin override rules
- financial and audit controls

This document must be locked early because it directly impacts:

- customer experience
- backend calculation logic
- dispatch and operations
- driver trust and retention
- finance reconciliation
- support workflows
- reporting and analytics

This document is intended to be the commercial source of truth for MVP implementation.

## 2. Scope

This document applies only to the MVP and only to in-scope services:

- Scheduled Local
- Scheduled One-Way Drop
- Scheduled Round Trip
- Airport Pickup/Drop
- Late-Night Safe Return

This document does not cover:

- subscriptions
- prepaid ride credits
- loyalty or wallet credits
- coupon systems
- dynamic AI-led pricing
- outstation multi-day pricing
- corporate contract pricing

## 3. Commercial Principles

The pricing and payout engine must satisfy the following principles.

### 3.1 Transparent to the Customer

- customers must see full fare components before booking
- no hidden charge may appear after trip completion
- route traffic alone should not create surprise charges on fixed-route services

### 3.2 Fair to the Driver

- drivers must see earning preview before accepting work
- drivers must be compensated for committed time and arrival effort
- platform rules must avoid arbitrary payout reduction after completion

### 3.3 Sustainable for the Platform

- pricing must not be subsidy-dependent
- below-floor pricing must require explicit approval
- cancellation and refund policies must protect both trust and unit economics

### 3.4 Simple Enough to Operate

- ops and support must be able to explain pricing in plain language
- customer support should not need to interpret formulas manually
- overrides should be rare, explicit, and auditable

### 3.5 Locked and Snapshot-Based

- every booking must store the quote snapshot used at confirmation
- pricing plan changes affect future quotes only, not historical bookings
- payout plan changes affect future assignments only, not already accepted work, unless an audited exception is approved

## 4. Decisions Locked by This Document

The following commercial decisions are considered locked for MVP unless formally revised:

- Rydvrse will use `scheduled-first pricing`
- `Lead time` affects customer fare
- `Night pricing` will be based on scheduled pickup time, not trip end time
- `Scheduled Local` and `Round Trip` will use a time-based model
- `One-Way Drop` will use a base fare plus return allowance model
- `Airport` will use fixed zone-based pricing
- `Late-Night Safe Return` is not a separate price engine; it is an overlay or label on supported services
- `Toll and parking` are not collected by the platform in MVP and are customer direct-pay unless ops explicitly handles an exception
- `Tip` is not a required MVP feature; if enabled later, it is 100% pass-through to the driver
- `No hidden post-trip charges` are allowed
- `Customer-confirmed trip start` remains mandatory
- `Admin overrides` are permission-scoped and audited

## 5. Commercial Glossary

### 5.1 Customer Fare Terms

- `Quote`: the time-limited pricing response shown before booking
- `Quote Snapshot`: the frozen pricing data attached to a confirmed booking
- `Base Fare`: the minimum charge for the service before add-ons and tax
- `Extension Charge`: extra charge for time-based bookings beyond quoted duration
- `Return Allowance`: extra fee on one-way services to compensate driver return effort
- `Lead-Time Fee`: additional fee for shorter-notice bookings
- `Night Surcharge`: charge applied when pickup time falls in the configured night band
- `Tax`: tax component applied according to the configured tax profile

### 5.2 Driver Payout Terms

- `Earning Preview`: estimated payout shown before assignment acceptance
- `Arrival Fee`: driver payout component for committing to and reaching pickup
- `Active Service Payout`: payout for performing the booked service time or route
- `Extension Payout`: extra payout for extra approved billable service time
- `Urgency Bonus`: driver bonus tied to short-notice rides if applicable
- `Night Bonus`: driver bonus for night bookings
- `Return Allowance Payout`: payout component passed to driver for one-way return effort

### 5.3 Cancellation and Refund Terms

- `Customer Cancellation`: cancellation initiated by customer before trip start
- `Customer No-Show`: customer not available after driver arrival and grace period
- `Driver Cancellation`: driver drops an accepted assignment before trip completion
- `Failed Fulfillment`: platform cannot provide service after booking confirmation
- `Refund`: money returned after payment or charge capture
- `Adjustment`: controlled commercial correction, usually handled internally and audited

## 6. Configuration Model

All commercial logic must be configuration-driven rather than hard-coded.

### 6.1 Pricing Configuration Dimensions

- city
- service zone
- service type
- lead-time bucket
- night-time window
- one-way distance or drop band
- airport zone band
- pricing plan version
- tax profile version

### 6.2 Payout Configuration Dimensions

- city
- zone or payout region
- service type
- lead-time bucket
- night-time window
- payout plan version

### 6.3 Policy Configuration Dimensions

- cancellation policy version
- refund policy version
- override permission matrix version

### 6.4 Snapshot Rules

- quote references pricing plan version
- booking stores quote snapshot
- accepted assignment stores payout preview snapshot
- completed trip stores final fare and final payout snapshot

## 7. Engine Architecture Overview

The commercial engine should be implemented as five coordinated sub-engines:

- `Quote Engine`
- `Final Fare Engine`
- `Payout Engine`
- `Cancellation Engine`
- `Refund and Override Engine`

### 7.1 Sequence Overview

1. Customer requests quote
2. Quote engine calculates customer-facing fare
3. Customer confirms booking
4. Booking stores quote snapshot
5. Dispatch creates assignment
6. Driver sees earning preview from payout engine
7. Trip executes
8. Final fare engine confirms locked fare and applies only eligible extra charges
9. Payment is collected
10. Payout engine finalizes driver ledger entry
11. Refund or adjustment engine applies only if needed

## 8. Core Inputs

The commercial engine may use only the following core inputs for MVP.

### 8.1 Quote Inputs

- customer city
- pickup zone
- drop zone if required
- service type
- scheduled pickup date/time
- expected duration for time-based services
- lead time bucket
- airport band if airport service
- one-way band if one-way drop

### 8.2 Final Fare Inputs

- quote snapshot
- trip start time
- trip end time
- actual ride state events
- approved customer-requested extension or route change events
- support or ops exceptions if authorized

### 8.3 Payout Inputs

- accepted assignment payout snapshot
- trip completion state
- final billable duration or fixed-route completion
- night band applicability
- urgency bucket
- one-way or airport payout band

### 8.4 Inputs Explicitly Excluded in MVP

- customer historical loyalty
- demand surge multiplier
- AI confidence score
- driver negotiation
- real-time market auctioning

## 9. Global Financial Rules

### 9.1 Calculation Precision

- all backend commercial calculations must be performed in paise
- UI may display rounded values, but invoices and ledgers must preserve exact calculated values

### 9.2 Currency

- all MVP amounts are in INR

### 9.3 Tax Handling

- tax must be applied by component according to configured tax rules
- customer quote and invoice must show tax as a visible line item or otherwise clearly include tax in total with disclosure
- tips, if enabled later, should not be treated as platform revenue

### 9.4 Rounding

- time-based quoted duration rounds up to the next 30-minute block
- extra time billing beyond quoted duration rounds up to the next 30-minute block after grace threshold
- customer-facing totals must reconcile with stored financial values

### 9.5 Quote Expiry

- all quotes must have an expiry timestamp
- expired quotes must not be bookable
- a fresh quote must use the then-current pricing plan

### 9.6 Plan Immutability

- quote plan and payout plan versions must be immutable references
- live config changes affect future quotes only

## 10. Lead-Time Buckets

Lead time is a pricing and payout input because short-notice fulfillment is operationally harder.

### 10.1 Standard Buckets

- `FLEX`: pickup scheduled `>= 180 minutes` from current time
- `PRIORITY`: pickup scheduled `60-179 minutes` from current time
- `EXPRESS`: pickup scheduled `< 60 minutes` from current time

### 10.2 MVP Recommendation

- `FLEX` enabled
- `PRIORITY` enabled
- `EXPRESS` disabled by default in launch city unless supply health supports it

### 10.3 Customer Effect

- `FLEX` = lowest fare
- `PRIORITY` = additional short-notice fee
- `EXPRESS` = premium fee if enabled

### 10.4 Driver Effect

- `FLEX` = no urgency bonus
- `PRIORITY` = urgency bonus
- `EXPRESS` = higher urgency bonus if enabled

## 11. Night-Time Rules

### 11.1 Night Band Definition

- default MVP night band: `22:00 to 06:00`, local city time

### 11.2 Trigger Rule

- night surcharge applies if `scheduled pickup time` falls within the night band

### 11.3 Locked Reasoning

Night pricing based on scheduled pickup time is easier to explain and avoids surprise billing caused by trip-end timing uncertainty.

### 11.4 Payout Effect

- if night surcharge applies to the booking, a defined `night bonus` applies to driver payout

## 12. Service-Specific Fare Design

## 12.1 Scheduled Local

### Model

- time-based
- minimum billable duration
- extension blocks beyond quoted duration

### Default Rules

- minimum quoted duration: `90 minutes`
- time increment block: `30 minutes`
- quote duration = `max(90, round_up_30(requested_duration))`
- final fare never reduces below quoted fare if actual duration is shorter
- extra extension charges apply only if actual service time exceeds quoted duration by more than the grace threshold

### Grace Threshold

- default extra-time grace threshold: `10 minutes`

### Customer Fare Formula

`quoted_service_fee_ex_tax = local_base_fee_90(zone, lead_bucket) + quote_extension_blocks * local_extension_fee_30(zone)`

Where:

- `quote_extension_blocks = (quoted_duration_minutes - 90) / 30`

### Final Fare Formula

`extra_minutes = max(actual_service_minutes - quoted_duration_minutes - grace_minutes, 0)`

`final_extra_blocks = ceil(extra_minutes / 30)`

`final_service_fee_ex_tax = quoted_service_fee_ex_tax + final_extra_blocks * local_extension_fee_30(zone)`

Then:

`final_customer_subtotal_ex_tax = final_service_fee_ex_tax + lead_time_fee + night_surcharge`

`final_customer_total = subtotal_ex_tax + tax`

### Allowed Extra-Charge Triggers

- actual service exceeds quoted duration by more than grace threshold
- customer explicitly requests extension or extra waiting

### Disallowed Extra-Charge Triggers

- normal traffic variation that remains within quoted duration
- system ETA error without actual overrun
- driver-side delay before trip start

## 12.2 Scheduled Round Trip

### Model

- same commercial engine as Scheduled Local for MVP
- separate service label for customer-facing UX and reporting

### Default Rules

- minimum quoted duration: `90 minutes`
- extension logic identical to Scheduled Local

### Reasoning

This avoids unnecessary engine complexity in MVP while preserving product clarity.

## 12.3 Scheduled One-Way Drop

### Model

- base time-backed fare plus explicit return allowance
- fixed route expectation

### Default Rules

- customer is quoted for the selected pickup and drop only
- pricing includes:
  - local base fee
  - return allowance by band
  - urgency fee if applicable
  - night surcharge if applicable
  - tax

### Customer Fare Formula

`quoted_service_fee_ex_tax = local_base_fee_90(zone, lead_bucket) + one_way_return_allowance(drop_band)`

`quoted_customer_total = quoted_service_fee_ex_tax + lead_time_fee + night_surcharge + tax`

### Final Fare Lock Rule

For One-Way Drop, the quoted fare is considered locked for the quoted route.

No extra charge may be added later for:

- traffic
- route ETA variance
- driver return difficulty already covered by allowance

Extra charge may be added only if:

- customer requested an additional stop
- customer requested extended waiting
- destination changed materially
- the above change is captured in app or through audited ops action

### Additional Stop / Change Rule

If a one-way booking is materially extended by customer request, the engine should convert the incremental part into time-based extension blocks using the standard extension fee for the booking's zone.

## 12.4 Airport Pickup/Drop

### Model

- zone-based fixed fare
- fixed route commercial product

### Default Rules

- airport fare determined by airport zone band and lead-time bucket
- night surcharge may apply
- fare includes standard route to or from airport
- traffic alone does not increase fare

### Customer Fare Formula

`quoted_service_fee_ex_tax = airport_fixed_fee(airport_zone_band, lead_bucket)`

`quoted_customer_total = quoted_service_fee_ex_tax + night_surcharge + tax`

### Final Fare Lock Rule

No additional customer fare may be added for:

- traffic delays
- normal airport congestion
- routing chosen by navigation

Additional charge may be added only if:

- customer requests stops
- customer changes destination materially
- customer requests waiting beyond airport courtesy threshold if such feature is enabled later

For MVP, it is recommended to keep airport pricing fixed and strict to reduce support load.

## 12.5 Late-Night Safe Return

### Model

- not a separate pricing engine
- label or entry path that maps to a supported base service

### Commercial Rule

Late-Night Safe Return uses the underlying service pricing plus applicable night surcharge.

Examples:

- a late-night short city ride uses Scheduled Local plus night surcharge
- a late-night one-way drop uses One-Way Drop plus night surcharge

## 13. Charge Components

## 13.1 Customer-Facing Components

- base service fee
- extension charge
- one-way return allowance
- airport fixed route fee
- priority fee
- express fee if enabled
- night surcharge
- tax

## 13.2 Driver-Facing Components

- arrival fee
- active service payout
- extension payout
- return allowance payout
- urgency bonus
- night bonus
- tip if enabled later

## 13.3 Platform Components

The platform retains economic value primarily from:

- base service fee margin
- part of active service fee
- part of urgency fee
- part of cancellation fee

The platform should not treat the following as margin:

- tax
- driver tip
- return allowance if configured as full driver pass-through

## 14. Reference Launch Pricing Pack

These are proposed reference values for the first city. They are not final legal or finance sign-off values, but they are the recommended launch defaults for engineering and product planning.

All values below are `ex tax` unless otherwise noted.

## 14.1 Lead-Time Fees

| Lead-Time Bucket | Customer Fee | Driver Bonus |
|---|---:|---:|
| FLEX | 0 | 0 |
| PRIORITY | 49 | 20 |
| EXPRESS | 99 | 40 |

MVP recommendation:

- launch with `FLEX` and `PRIORITY`
- keep `EXPRESS` feature-flagged off

## 14.2 Night Surcharge and Bonus

| Component | Value |
|---|---:|
| Customer Night Surcharge | 99 |
| Driver Night Bonus | 80 |

## 14.3 Scheduled Local / Round Trip Reference

| Component | Core Zone | Extended Zone |
|---|---:|---:|
| Base Fee for First 90 Minutes | 329 | 379 |
| Extension Fee Per 30 Minutes | 89 | 99 |

## 14.4 One-Way Return Allowance Reference

| Drop Band | Example Interpretation | Customer Return Allowance | Driver Return Allowance Payout |
|---|---|---:|---:|
| Band A | Same macro-zone / shortest return effort | 79 | 79 |
| Band B | Medium return effort | 129 | 129 |
| Band C | Long return effort | 179 | 179 |
| Band D | Furthest in-city return effort | 249 | 249 |

## 14.5 Airport Fixed Fare Reference

| Airport Zone Band | Customer Fixed Fare |
|---|---:|
| Zone A | 449 |
| Zone B | 549 |
| Zone C | 649 |

## 15. Fare Examples Using Reference Pack

All examples below exclude tax for readability.

### 15.1 Scheduled Local, Daytime, Flex, 90 Minutes

- Service: Scheduled Local
- Zone: Core
- Duration Quoted: 90 min
- Lead Bucket: Flex
- Night: No

Customer:

- Base Fee: 329
- Lead-Time Fee: 0
- Night: 0
- Total Ex Tax: `329`

### 15.2 Scheduled Local, Night, Priority, 120 Minutes

- Service: Scheduled Local
- Zone: Core
- Duration Quoted: 120 min
- Lead Bucket: Priority
- Night: Yes

Customer:

- Base Fee 90 Min: 329
- One Extra 30-Min Block: 89
- Priority Fee: 49
- Night Surcharge: 99
- Total Ex Tax: `566`

### 15.3 One-Way Drop, Daytime, Flex, Band B

- Service: One-Way Drop
- Zone: Core
- Return Band: B
- Lead Bucket: Flex
- Night: No

Customer:

- Base Fee 90 Min: 329
- Return Allowance: 129
- Total Ex Tax: `458`

### 15.4 Airport Drop, Night, Priority, Zone B

- Service: Airport
- Zone Band: B
- Lead Bucket: Priority
- Night: Yes

Customer:

- Airport Fixed Fee: 549
- Priority Fee: 49
- Night Surcharge: 99
- Total Ex Tax: `697`

## 16. Driver Payout Engine

## 16.1 Payout Principles

- drivers see estimated earnings before acceptance
- payout must not drop below accepted quoted work unless a policy-defined event changes the commercial scope
- driver should be compensated for:
  - accepting and reaching pickup
  - completing the booked service
  - short-notice urgency
  - one-way return effort where applicable
  - night work

## 16.2 Payout Lock Rules

- accepted assignment stores earning preview snapshot
- if the trip completes within quoted commercial scope, driver receives at least preview payout
- if billable extension occurs, driver receives incremental extension payout
- if customer cancels after driver has materially committed, cancellation compensation applies as defined below

## 16.3 Reference Payout Configuration

All values below are proposed launch defaults and may be tuned by city.

### Local / Round Trip Payout Reference

| Component | Core Zone | Extended Zone |
|---|---:|---:|
| Arrival Fee | 60 | 70 |
| Active Service Payout for First 90 Minutes | 150 | 160 |
| Extension Payout Per 30 Minutes | 50 | 55 |
| Priority Bonus | 20 | 20 |
| Express Bonus | 40 | 40 |
| Night Bonus | 80 | 80 |

### Airport Payout Reference

| Airport Zone Band | Arrival Fee | Airport Active Service Payout |
|---|---:|---:|
| Zone A | 60 | 150 |
| Zone B | 60 | 180 |
| Zone C | 60 | 220 |

### One-Way Return Allowance Payout

- return allowance payout = `100% of configured customer return allowance`

## 16.4 Scheduled Local / Round Trip Payout Formula

For quoted work:

`quoted_payout = arrival_fee + active_service_payout_90 + quote_extension_blocks * extension_payout_30 + urgency_bonus + night_bonus`

For final payout after completion:

`final_payout = quoted_payout + final_extra_blocks * extension_payout_30 + tip_if_any`

Where:

- `final_extra_blocks` are only the approved billable extension blocks beyond quoted duration

## 16.5 One-Way Drop Payout Formula

`quoted_payout = arrival_fee + active_service_payout_90 + return_allowance_payout + urgency_bonus + night_bonus`

If the route is materially extended by approved customer request:

`final_payout = quoted_payout + approved_extension_blocks * extension_payout_30 + tip_if_any`

## 16.6 Airport Payout Formula

`quoted_payout = arrival_fee + airport_active_service_payout(zone_band) + urgency_bonus + night_bonus`

If the airport ride is materially extended by approved customer request:

`final_payout = quoted_payout + approved_extension_blocks * extension_payout_30 + tip_if_any`

## 16.7 Driver Preview Examples Using Reference Pack

### Local, Daytime, Flex, 90 Minutes, Core Zone

- Arrival Fee: 60
- Active Service Payout: 150
- Urgency Bonus: 0
- Night Bonus: 0
- Preview Payout: `210`

### Local, Night, Priority, 120 Minutes, Core Zone

- Arrival Fee: 60
- Active Service Payout 90 Min: 150
- One Extension Block: 50
- Priority Bonus: 20
- Night Bonus: 80
- Preview Payout: `360`

### One-Way Drop, Daytime, Flex, Band B, Core Zone

- Arrival Fee: 60
- Active Service Payout 90 Min: 150
- Return Allowance Payout: 129
- Total Preview Payout: `339`

### Airport, Night, Priority, Zone B

- Arrival Fee: 60
- Airport Active Service Payout: 180
- Priority Bonus: 20
- Night Bonus: 80
- Total Preview Payout: `340`

## 17. Customer Cancellation Charges

The cancellation engine must balance fairness to customers with protection for driver time and platform operations.

## 17.1 Customer Cancellation Rules

- customer must always see the fee outcome before confirming cancellation
- cancellation outcome depends on booking stage and timing
- after trip start, booking cancellation is not a valid path; the trip must instead end normally or route through support

## 17.2 Cancellation Stages

### Stage A: Before Assignment

- customer charge: `0`
- driver compensation: `0`
- platform action: release booking

### Stage B: After Assignment, More Than 60 Minutes Before Pickup

- customer charge: `0`
- driver compensation: `0`
- platform action: release driver and booking

### Stage C: After Assignment, Within 60 Minutes Before Pickup, Driver Not Yet Arrived

- proposed customer fee: `79`
- proposed driver compensation: `40`
- platform retains remainder to cover ops and payment cost

### Stage D: Driver Arrived, Customer Cancels Before Trip Start

- proposed customer fee: `149`
- proposed driver compensation: `80`
- platform retains remainder to cover ops and platform cost

### Stage E: Customer No-Show After Driver Arrival and Grace Period

- treat same as Stage D by default
- customer fee: `149`
- driver compensation: `80`

### Stage F: After Trip Start

- customer cancellation path disabled
- trip moves through normal completion or support-led exception handling
- customer owes at least the booked commercial minimum

## 17.3 Cancellation Grace Period for No-Show at Pickup

- recommended pickup no-show grace period: `10 minutes`

Before customer no-show fee is applied:

- driver must mark arrived
- system must capture arrival timestamp
- customer should receive arrival notification
- supportable evidence should exist if disputed

## 17.4 Customer Cancellation Fee Table

| Cancellation Stage | Customer Fee | Driver Compensation | Platform Retained |
|---|---:|---:|---:|
| Before Assignment | 0 | 0 | 0 |
| After Assignment, >60 Min Before Pickup | 0 | 0 | 0 |
| Within 60 Min, Driver Not Arrived | 79 | 40 | 39 |
| After Driver Arrival, Before Start | 149 | 80 | 69 |
| Customer No-Show at Pickup | 149 | 80 | 69 |

## 17.5 Customer Cancellation Fee Notes

- taxes on cancellation fees must follow the configured tax profile
- displayed cancellation outcome to the customer must include final payable amount as shown in the UI
- support may waive or reduce cancellation fees only under policy-defined override rules

## 18. Driver Cancellation Rules

Driver cancellation impacts customer trust more severely than customer cancellation.

## 18.1 Customer Impact Rule

- customer must never be charged a cancellation fee because of driver cancellation

## 18.2 Driver Cancellation Outcomes

### Driver Cancels Before 60 Minutes to Pickup

- no payout
- booking re-enters assignment flow
- driver reliability score impact

### Driver Cancels Within 60 Minutes to Pickup

- no payout
- stronger reliability score impact
- may affect priority access to future assignments or incentive eligibility

### Driver No-Show or Severe Delay Causing Failed Fulfillment

- no payout
- strong operational penalty
- customer is owed no-charge resolution and possible goodwill handling

### Driver Cancels After Arrival Without Approved Reason

- no payout
- severe penalty
- escalated review

### Driver Cancels for Verified Emergency

- no punitive commercial action
- booking still rescued if possible

## 18.3 Recommendation on Driver Penalties

For MVP, avoid complex automatic monetary deductions. Use:

- reliability scoring
- dispatch deprioritization
- incentive ineligibility
- suspension review for repeated behavior

This is simpler, less legally risky, and easier to explain.

## 19. Final Fare Adjustment Rules

To preserve trust, only a narrow set of adjustments are allowed after booking confirmation.

## 19.1 Allowed Positive Adjustments

- approved extra time beyond quoted duration for time-based services
- approved customer-requested stops or route changes
- audited correction of a missed valid charge that the customer already acknowledged during service

## 19.2 Disallowed Positive Adjustments

- extra fee due only to traffic
- extra fee due only to driver getting lost
- extra fee due to slow assignment
- retroactive night surcharge because trip ended late
- retroactive change caused by pricing plan edits after booking

## 19.3 Allowed Downward Adjustments

- support-approved goodwill reduction
- pricing engine error correction
- service failure compensation
- manual adjustment approved under override policy

## 19.4 Minimum Charge Rule

For completed time-based bookings:

- final fare may not drop below the quoted booked duration charge except through approved refund or override workflow

## 20. Refund Engine

Refunds must be category-based, auditable, and bounded by approval rules.

## 20.1 Refund Principles

- refunds are not free-form guesses
- every refund requires a reason category
- every refund must link to a booking, payment, or incident
- auto-refund is preferred for clearly technical cases
- manual refunds require role-appropriate approval

## 20.2 Refund Categories

- failed fulfillment
- duplicate charge
- payment technical issue
- pricing mismatch
- driver delay
- driver no-show
- wrong driver / trust issue
- service quality issue
- safety incident
- goodwill exception

## 20.3 Refund Decision Matrix

| Category | Default Outcome | Default Refund Range | Default Owner |
|---|---|---:|---|
| Failed Fulfillment Before Start | full refund of any collected amount | 100% | Ops / Support |
| Duplicate Charge | refund duplicate amount | 100% of duplicate | Finance |
| Payment Technical Error | refund incorrect capture | case exact | Finance |
| Pricing Mismatch / Engine Error | refund overcharged amount | exact difference or more | Support Lead / Finance |
| Driver Delay, Ride Completed | goodwill partial refund | 49 to 99 or up to 20% | Support |
| Driver No-Show | full refund of any collected amount | 100% | Ops |
| Wrong Driver / Trust Breach Before Start | full refund if prepaid | up to 100% | Ops Lead |
| Service Quality Issue After Completion | partial or full depending on severity | 10% to 100% | Support Lead |
| Safety Incident / Verified Misconduct | refund platform-collected fare at minimum | 50% to 100% | Ops Lead / Super Admin |
| Goodwill Exception | bounded discretionary refund | capped by role | Support Lead |

## 20.4 Refund SLA Recommendations

- technical duplicate refunds: initiate within `2 hours`
- support-approved standard refunds: initiate within `24 hours`
- severe incident-linked refunds: initiate after decision but no later than `24 hours` once approved

## 20.5 Partial Refund Recommendation Rules

### Delay-Based Partial Refund

Default recommendation:

- if ride completed but delay materially harmed experience and no better recovery was offered:
  - `49` or `99` goodwill refund
  - or up to `20% of service fee ex tax`, whichever is lower

### Service Quality Partial Refund

Default recommendation:

- mild issue: `10-20%`
- moderate issue: `20-50%`
- severe verified misconduct: `50-100%`

## 20.6 Non-Refundable by Default

These should not trigger automatic refunds:

- normal traffic
- customer simply changed mind after completion
- dissatisfaction without service failure or policy basis

## 21. Admin Override Rules

Overrides exist for controlled exception handling, not daily operation.

## 21.1 Allowed Override Types

- waive customer cancellation fee
- reduce customer final fare
- issue partial refund
- issue full refund
- correct payout under investigation
- apply manual reassignment
- update future pricing config

## 21.2 Prohibited Override Types

- retroactively increase final customer fare without auditable customer-approved basis
- delete a quote snapshot
- delete a payout record after settlement
- silently alter tax values without configuration change and audit
- charge customer for driver-caused cancellation
- create negative driver payout without approved policy-backed deduction model

## 21.3 Role-Based Override Matrix

| Role | Allowed Actions | Not Allowed |
|---|---|---|
| Support L1 | create ticket, recommend refund, request cancellation fee waiver | direct high-value refund approval, pricing edits, retro fare increase |
| Ops Executive | manual reassignment, mark failed fulfillment, request fee waiver | pricing config edits, unrestricted refund approval |
| Support Lead | approve small partial refunds, waive cancellation fees within cap | pricing plan edits, retro fare increase |
| Finance Admin | process approved refunds, resolve payment technical issues | manual dispatch changes, pricing plan edits |
| City Admin | update future pricing/serviceability config, view reporting | retroactively edit completed booking fare without override policy |
| Super Admin / Head Ops | emergency override, full refund approval, exceptional payout correction | deletion of audit history |

## 21.4 Recommended Approval Caps

These caps are recommended starting points.

| Role | Max Single Refund Approval | Cancellation Fee Waiver | Payout Correction |
|---|---:|---:|---:|
| Support L1 | 0 direct approval | 0 direct approval | 0 |
| Support Lead | 300 or 25% of fare, whichever is lower | full waiver | 0 |
| Finance Admin | approved amount only | no discretionary waiver | approved correction only |
| Ops Lead / Super Admin | full amount when justified | full waiver | full correction when audited |

## 21.5 Override Audit Requirements

Every override must record:

- actor
- role
- timestamp
- booking or payout entity
- previous value
- new value
- reason code
- free-text note
- linked ticket or incident ID if applicable

## 22. Financial Controls and Guardrails

## 22.1 Pricing Floor Rules

No pricing change may go live if it pushes a service below the configured floor without explicit finance and product approval.

### Recommended Floor Logic

- each service/zone combination has:
  - customer minimum price floor
  - driver minimum payout floor
  - platform minimum gross margin warning threshold

## 22.2 Payout Floor Rules

- no completed trip should settle below the configured driver minimum guarantee for that service/zone unless the trip never started and policy says otherwise

## 22.3 Margin Monitoring

The business should report:

- average customer service fee ex tax
- average driver payout
- average refund amount
- average cancellation revenue
- contribution margin trend per service type

## 22.4 Pass-Through Components

These should not be treated as platform margin:

- return allowance if configured as 100% driver pass-through
- tip
- tax

## 22.5 Booking Snapshot Integrity

- quote snapshot must never be mutated after booking
- final fare adjustments must be additive or corrective entries, not silent overwrites

## 23. Suggested Data Model

The pricing and payout engine should at minimum store the following entities.

### 23.1 Pricing Entities

- `pricing_plan`
- `pricing_rule`
- `pricing_zone`
- `lead_time_bucket`
- `airport_zone_band`
- `one_way_band`
- `quote`
- `quote_component`
- `booking_fare_snapshot`
- `fare_adjustment`

### 23.2 Payout Entities

- `payout_plan`
- `driver_payout_preview`
- `driver_payout_component`
- `driver_payout_snapshot`
- `driver_earning_ledger`
- `driver_payout_batch`

### 23.3 Cancellation and Refund Entities

- `cancellation_policy`
- `cancellation_charge`
- `refund_request`
- `refund_decision`
- `refund_transaction`
- `override_audit`

## 24. Customer Notification Triggers

The commercial engine should trigger or support the following customer-visible notifications:

- quote expired
- booking confirmed
- cancellation successful
- cancellation fee waived or changed
- payment failed
- payment successful
- refund initiated
- refund completed

## 25. Driver Notification Triggers

- earnings preview available with job offer
- assignment cancelled
- payout settled
- payout correction applied
- cancellation compensation applied

## 26. Operational Scenarios

## 26.1 Platform Failed Fulfillment Before Start

- customer pays: `0` or any collected amount refunded in full
- driver payout: `0`
- ops may grant goodwill manually if needed

## 26.2 Customer Cancels Within 60 Minutes of Pickup

- customer fee: `79`
- driver compensation: `40`
- booking closed as customer-cancelled

## 26.3 Customer Cancels After Driver Arrival

- customer fee: `149`
- driver compensation: `80`
- support may waive fee if arrival issue is platform fault

## 26.4 Driver Cancels Within 60 Minutes of Pickup

- customer fee: `0`
- driver payout: `0`
- reliability penalty applied
- ops begins rescue if customer still wants ride

## 26.5 Payment Failed After Trip Completion

- trip remains completed
- fare remains visible
- payment status = failed or pending
- customer can retry
- support can investigate if capture state is unclear

## 26.6 Pricing Mismatch Discovered Post-Trip

- support compares:
  - quote snapshot
  - final fare components
  - adjustment history
- if overcharge exists:
  - refund exact difference
  - escalate if systemic

## 27. Acceptance Criteria for Commercial Engine

The commercial engine is MVP-ready only if all of the following are true:

- quote generation reflects service type, zone, lead time, and night rules correctly
- every confirmed booking stores an immutable quote snapshot
- customers can see cancellation outcome before confirming cancellation
- fixed-route services do not gain surprise traffic-based charges
- drivers see earning preview before acceptance
- driver payout snapshots are preserved for completed trips
- refunds require reason codes and are audited
- override permissions enforce role limits
- pricing changes affect future bookings only
- finance can reconcile customer charge, refund, and driver payout records cleanly

## 28. Open Values to Finalize Before Go-Live

This document locks the engine design, but the following numeric values still require business and finance sign-off before production:

- final launch city reference price pack
- final GST handling by component if legal review requires changes
- exact one-way band definitions by distance or zone map
- exact airport zone band boundaries
- exact approval caps by internal role
- whether `EXPRESS` is enabled at launch
- whether tips are in MVP or delayed

## 29. Summary

This document defines the Rydvrse MVP commercial engine as a configuration-driven, trust-first system built around:

- transparent customer pricing
- fair driver payouts
- predictable cancellation outcomes
- structured refunds
- tightly controlled admin overrides

If implemented as written, this engine will support:

- cleaner product behavior
- lower support ambiguity
- better driver trust
- stronger finance reconciliation
- more disciplined unit economics than a loosely defined service marketplace

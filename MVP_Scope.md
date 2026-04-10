# Rydvrse MVP Scope Freeze

## Document Control

- Document Name: `MVP_Scope.md`
- Product: `Rydvrse`
- Version: `1.0`
- Status: `Frozen`
- Freeze Date: `2026-04-10`
- Parent Reference: `High_Level_Design.md`
- Purpose: `Define the exact MVP scope, exclusions, and release gates for Rydvrse so product, engineering, design, and operations build against one shared boundary.`

## 1. Scope Freeze Statement

This document freezes the MVP scope for Rydvrse.

The MVP is intentionally constrained to protect capital, reduce execution risk, and reach market with a reliable service instead of an overloaded feature set.

Anything not explicitly marked as `In Scope` in this document must be treated as `Out of Scope` for MVP unless it is approved through formal scope change.

## 2. MVP Objective

The Rydvrse MVP exists to validate one core hypothesis:

`Customers who own cars will pay for a reliable, verified, and transparently priced scheduled driver service if the experience is more trustworthy and operationally dependable than alternatives.`

The MVP is not intended to prove every possible business line. It is intended to prove:

- repeat demand exists
- customers trust the booking model
- drivers accept the payout model
- the operations model can fulfill rides reliably
- unit economics can remain healthy without heavy subsidy

## 3. MVP Positioning

Rydvrse MVP will be positioned as:

`A scheduled-first private driver platform for your own car, focused on clear pricing, trusted drivers, and reliable fulfillment.`

## 4. Scope Principles

All MVP decisions must follow these principles:

- `Reliability over breadth`
- `Transparency over promotional complexity`
- `Scheduled-first over instant-demand chaos`
- `Ops-assisted service quality over premature automation`
- `Positive unit economics over growth-by-burn`
- `Private car driver service over multi-model mobility expansion`

## 5. Launch Constraints

The following constraints are fixed for MVP:

- Launch in `one city only`
- Start in `2-3 dense micro-zones`
- Support `private car owner use cases only`
- No owned vehicle supply
- No multi-city operations
- No broad instant-booking promise across the full city
- No aggressive cashback or subsidy strategy
- No microservices-first architecture

## 6. Target Users

### 6.1 Primary Customer Segments

- urban professionals with private cars
- family coordinators booking for spouse, parents, or guests
- airport travelers using their own car
- late-night return users prioritizing trust and safety
- repeat users needing reliable planned local travel

### 6.2 Primary Driver Segment

- independent licensed drivers seeking scheduled rides
- drivers who value predictable assignments and transparent payouts
- drivers comfortable with app-based booking and navigation

### 6.3 Internal Users

- operations executives
- city manager
- support team
- onboarding and compliance reviewer
- finance/admin reviewer

## 7. Launch Geography Scope

### 7.1 Geography Included

- one launch city only
- 2-3 high-density operating zones within that city

### 7.2 Geography Excluded

- secondary city launch
- nationwide availability
- intercity multi-day launch coverage
- low-density, hard-to-serve fringe areas in the first release

### 7.3 Launch Geography Assumption

The exact city and zone names may be finalized in a separate go-to-market document, but this does not change the scope boundary:

- MVP remains `single-city`
- MVP remains `micro-zone first`

## 8. Service Scope

## 8.1 In Scope Service Types

### A. Scheduled Local

Use case:

- errands
- meetings
- shopping
- office commute
- local family travel

Definition:

- customer books a driver for local city movement in their own car
- booking is scheduled in advance
- fare is time-based with a defined minimum duration and transparent extensions

### B. Scheduled One-Way Drop

Use case:

- office drop
- event drop
- station or home drop
- airport-adjacent one-way use cases

Definition:

- customer books a driver to move their car and themselves from point A to point B
- pricing includes local service plus clearly disclosed return allowance logic

### C. Scheduled Round Trip

Use case:

- meetings with wait time
- event attendance
- multi-stop city errands

Definition:

- customer uses one booking for travel with return or continued service within the allowed duration window

### D. Airport Pickup/Drop

Use case:

- airport drop in customer car
- airport pickup in customer car

Definition:

- fare may be zone-fixed rather than fully time-based
- airport service follows a simplified, easy-to-understand pricing model

### E. Late-Night Safe Return

Use case:

- safe drive home after events, dinners, or late work hours

Definition:

- scheduled or constrained-priority ride for late-night safety use cases
- includes night surcharge rules and stronger support visibility

## 8.2 Out of Scope Service Types

- chauffeur-driven rental car inventory
- self-drive rentals
- outstation multi-day packages
- wedding/event luxury chauffeur verticalization
- white-labeled corporate transport solution
- dedicated employee shuttle operations
- subscription-only service without pay-per-booking

## 9. Platform Scope

## 9.1 Customer App In Scope

- OTP signup/login
- location permission and city setup
- home screen with supported service types
- quote generation
- booking confirmation
- upcoming booking list
- booking detail screen
- assigned driver profile view
- live driver approach tracking
- customer-confirmed trip start
- trip progress screen
- live share
- SOS
- support access
- trip completion summary
- payment completion
- invoice view
- rating and issue reporting
- cancellation and modification per policy

## 9.2 Customer App Out of Scope

- wallet with stored balance
- cashback engine
- loyalty tiers
- subscription purchase flow
- multilingual voice assistant
- AI concierge
- family profile hierarchy with deep permission models
- in-app chatbots
- promotional game mechanics

## 9.3 Driver App In Scope

- OTP login
- onboarding profile creation
- document upload
- onboarding status view
- availability control
- scheduled jobs list
- accept/decline assignment
- navigation handoff
- mark arrived
- wait for customer start confirmation
- trip progress and completion
- earnings summary
- issue reporting
- support access

## 9.4 Driver App Out of Scope

- complex gamified incentive engine
- community features
- in-app wallet for loans/advances
- built-in training academy with advanced content
- deep multilingual content management
- automated tax filing features

## 9.5 Ops/Admin Platform In Scope

- booking queue
- assignment monitor
- reassignment controls
- delayed booking rescue
- support ticket management
- incident case management
- refund management
- driver onboarding approval
- pricing rule management
- city/zone serviceability configuration
- basic operational dashboards
- user and driver profile review
- audit trail access

## 9.6 Ops/Admin Platform Out of Scope

- multi-tenant enterprise admin
- full B2B account management
- custom SLA billing engines
- BI-grade advanced analytics suite
- workforce planning automation at enterprise scale

## 10. Core User Journeys in Scope

The MVP must fully support these journeys end-to-end.

### 10.1 Customer First-Time Booking

1. install app
2. OTP login
3. select service type
4. provide pickup and schedule
5. view quote
6. confirm booking
7. receive assignment
8. track driver arrival
9. confirm trip start
10. complete trip
11. pay
12. rate driver

### 10.2 Returning Customer Rebooking

1. open app
2. choose previous or saved flow
3. confirm ride details
4. book
5. complete trip

### 10.3 Driver Onboarding

1. OTP login
2. profile entry
3. document upload
4. manual ops review
5. approved or rejected status
6. first trip eligibility

### 10.4 Driver Assigned Ride

1. receive booking offer
2. review earnings and ride details
3. accept
4. navigate to pickup
5. mark arrived
6. wait for trip start confirmation
7. complete trip
8. view earnings summary

### 10.5 Ops Rescue Flow

1. booking enters risk state
2. ops receives alert
3. ops reassigns or intervenes
4. customer is informed if needed
5. ride is recovered or marked failed

### 10.6 Support and Incident Flow

1. customer or driver raises issue
2. ticket is categorized
3. ride context is attached
4. support or ops investigates
5. refund, warning, or closure action is applied

## 11. Core Business Capabilities in Scope

### 11.1 Booking Capability

- create, view, update, and cancel bookings
- support future-dated scheduling
- track booking states

### 11.2 Quoting and Pricing Capability

- compute transparent quote
- persist quote snapshot with booking
- support city and zone rules
- support night charges and return allowance

### 11.3 Dispatch Capability

- filter eligible drivers
- score or prioritize candidates
- assign driver
- reassign driver
- surface risk to ops

### 11.4 Trip Execution Capability

- arrival state
- customer start confirmation
- live trip state
- trip completion
- trip event tracking

### 11.5 Payment Capability

- collect payment
- record payment status
- generate invoice
- initiate refunds

### 11.6 Support Capability

- create tickets
- view ticket context
- track case status
- record resolution actions

### 11.7 Driver Operations Capability

- approve drivers
- manage driver status
- view document health
- monitor performance basics

## 12. Pricing Scope Freeze

Pricing for MVP is frozen at the model level even if exact numeric values are later tuned.

### 12.1 In Scope Pricing Structure

- scheduled local with minimum duration and extension slabs
- one-way drop with transparent return allowance logic
- airport fixed fare by zone or zone band
- night surcharge
- cancellation fee logic
- admin-approved manual override with audit log

### 12.2 Out of Scope Pricing Structure

- surge pricing with opaque multipliers
- dynamic AI-led pricing
- wallet-linked pricing
- subscription-led booking discounts
- loyalty points redemption
- city-wide promotional pricing matrix explosion

## 13. Driver Payout Scope Freeze

### 13.1 In Scope

- earning preview before ride acceptance
- payout based on defined components
- visibility into completed trip earnings
- payout ledger

### 13.2 Out of Scope

- gamified quest system
- driver loans or financial products
- auto-generated tax optimization
- complex badge-based pay tiers

## 14. Trust and Safety Scope Freeze

## 14.1 In Scope

- verified driver profile
- masked call/contact
- live tracking
- trip sharing
- SOS
- support escalation
- trip-start confirmation by customer
- incident reporting and evidence handling

## 14.2 Out of Scope

- connected dashcam integration
- telematics hardware deployment
- in-car IoT device installation
- third-party insurance marketplace aggregation
- advanced biometric ride authentication

## 15. Data and Analytics Scope Freeze

## 15.1 In Scope

- booking analytics
- fulfillment analytics
- trip completion analytics
- refund analytics
- driver earnings analytics
- support category analytics

## 15.2 Out of Scope

- enterprise data warehouse program
- self-serve analytics platform
- advanced predictive BI
- full experimentation framework

## 16. Technical Scope Freeze

## 16.1 In Scope

- Spring Boot modular monolith backend
- PostgreSQL primary database
- Redis cache and ephemeral state
- React Native customer app
- React Native driver app
- web-based admin and ops console
- push notifications
- OTP authentication
- payment gateway integration
- map/geocoding integration
- object storage for documents and evidence
- basic observability and audit logging

## 16.2 Out of Scope

- microservices decomposition
- Kubernetes-heavy platform engineering if not required
- event streaming platform at enterprise scale
- data lake program
- custom ML infrastructure

## 17. Operational Scope Freeze

## 17.1 In Scope

- manual driver onboarding review
- manual assignment rescue
- support-led refund approval
- city and zone serviceability management
- driver compliance checks
- daily operational monitoring

## 17.2 Out of Scope

- full dispatch automation with no human intervention
- 24x7 national command center operations
- large multi-layer escalation org
- outsourced enterprise BPO design

## 18. Explicit MVP Exclusions

The following items are explicitly deferred and must not be added to the MVP backlog unless scope is reopened:

- subscriptions
- prepaid ride bundles
- family account hierarchy
- preferred drivers
- recurring commute automation
- corporate dashboard
- corporate invoicing workflows
- AI allocation engine
- AI ETA optimization
- loyalty rewards
- wallet
- coupons and cashback engine
- multi-language content beyond a practical baseline
- chatbots
- outstation multi-day service
- rented car with driver inventory
- white-label partner portals
- advanced experimentation platform

## 19. Launch Readiness Criteria

The MVP is considered build-complete only if all critical launch criteria are met.

### 19.1 Product Readiness

- customer can complete end-to-end booking flow
- driver can complete end-to-end assigned trip flow
- ops can rescue failed assignments
- support can resolve core ticket categories
- pricing is visible and understandable

### 19.2 Operational Readiness

- driver onboarding SOP exists
- assignment rescue SOP exists
- cancellation and refund SOP exists
- incident escalation SOP exists
- launch zones and hours are configured

### 19.3 Technical Readiness

- production deployment pipeline exists
- error monitoring is enabled
- critical audit logs are persisted
- payment flow works reliably
- notification delivery is stable

### 19.4 Business Readiness

- pricing and payout values approved
- refund policy approved
- launch supply available in selected zones
- support ownership is assigned

## 20. MVP Success Metrics

The MVP does not need to dominate the market. It needs to prove service viability.

Primary success signals:

- strong fulfillment rate
- repeat usage within 30 days
- acceptable support ticket rate
- manageable refund rate
- positive or improving contribution margin trend
- enough driver retention to support launch zones

## 21. Change Control Rules

To prevent scope creep, the following rules apply:

### 21.1 Rule 1

Any feature addition to MVP must identify:

- why it is critical for launch
- what existing scope item is removed or deferred
- what engineering and ops cost it adds

### 21.2 Rule 2

Features requested for branding, investor optics, or parity optics alone do not qualify for MVP unless they directly improve launch viability.

### 21.3 Rule 3

No major new service line may be introduced into MVP after this freeze.

### 21.4 Rule 4

No architecture expansion may be approved unless the current design cannot support launch requirements.

## 22. Decisions Frozen by This Document

The following decisions are locked:

- Rydvrse MVP is `scheduled-first`
- Rydvrse MVP is `single-city`
- Rydvrse MVP serves `private car owners`
- the backend is a `modular monolith`
- ops tooling is part of MVP
- customer-confirmed trip start is part of MVP
- pricing transparency is mandatory
- multi-service expansion is deferred
- subscription and loyalty systems are deferred
- corporate expansion is deferred

## 23. Post-MVP Candidate Items

These are valid candidates for Phase 2 and beyond, but are not part of MVP:

- recurring commute booking
- prepaid credits
- preferred drivers
- family account support
- corporate-lite plans
- richer analytics
- loyalty and retention systems
- AI dispatch optimization
- multi-city readiness tooling

## 24. Summary

The Rydvrse MVP is a deliberately narrow product:

`a reliable, transparently priced, scheduled driver booking platform for private car owners in one city.`

This is the correct scope for a low-burn launch because it:

- reduces capital risk
- simplifies operations
- improves service quality
- helps achieve repeat behavior faster
- creates room to expand later from a strong foundation

This document should be treated as the operational scope contract for the MVP.

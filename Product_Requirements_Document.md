# Rydvrse Product Requirements Document

## Document Control

- Document Name: `Product_Requirements_Document.md`
- Product: `Rydvrse`
- Version: `1.0`
- Status: `Baseline PRD for MVP`
- Last Updated: `2026-04-10`
- Source Documents:
  - `High_Level_Design.md`
  - `MVP_Scope.md`
- Audience:
  - founders
  - product management
  - engineering
  - design
  - operations
  - support
  - finance
  - QA and release management

## 1. Purpose

This PRD turns the Rydvrse high-level design and frozen MVP scope into exact product and business requirements for build execution.

This document exists to answer the following questions clearly:

- what the MVP must do
- what the MVP must not do
- who the MVP is for
- what user journeys must work at launch
- what business rules govern pricing, booking, dispatch, and support
- how success will be measured
- what acceptance criteria define release readiness

This PRD is the primary product contract for the MVP. Engineering, design, operations, and QA should use it as the reference point for implementation and validation.

## 2. Product Summary

Rydvrse is a `scheduled-first private driver platform` for people who already own cars and want to book a verified driver to operate their own vehicle.

The MVP is designed to compete on:

- pricing transparency
- trust and safety
- reliable fulfillment
- better trip-start controls
- clearer support and incident handling

The MVP is intentionally constrained for low-burn execution. It is not trying to be a large multi-service mobility platform at launch.

## 3. Problem Statement

Customers in the private-driver market face several recurring problems:

- uncertainty about whether a driver will actually be assigned on time
- unclear or surprising final billing
- weak visibility into driver quality and verification
- poor support during delays or disputes
- anxiety when the driver starts the trip before the customer is ready

Drivers face a different but equally important set of problems:

- unclear payouts
- weak visibility into incentives and earnings
- unpredictable ride quality
- poor support during disputes
- lack of trust in platform rules

Operations teams typically struggle with:

- last-minute assignment failures
- weak tools for reassignment rescue
- inconsistent refund decisions
- missing evidence in disputes
- fragmented visibility into booking health

Rydvrse addresses this by building a booking and trip system that is trust-first, scheduled-first, and operationally controlled.

## 4. Product Vision and MVP Thesis

### 4.1 Vision

Rydvrse will become the most reliable and transparent way to hire a verified driver for your own car.

### 4.2 MVP Thesis

The MVP is intended to validate this hypothesis:

`Customers will repeatedly use and pay for a scheduled private-driver platform if the service is more transparent, more dependable, and operationally safer than current alternatives.`

### 4.3 Positioning Statement

`Rydvrse is the most reliable way to book a verified driver for your own car, with clear pricing and stronger trip control.`

## 5. Business Goals

### 5.1 Primary Business Goals

- validate repeat demand in a focused launch city
- prove that transparency can convert and retain customers
- prove that drivers will participate under a fair payout model
- achieve operationally manageable fulfillment within dense launch zones
- protect capital by avoiding subsidy-heavy growth

### 5.2 Secondary Business Goals

- create the foundation for future recurring commute and prepaid credit products
- build a trust brand from day one
- establish enough data quality for later optimization and analytics

## 6. Goals and Non-Goals

### 6.1 MVP Goals

- launch a customer app, driver app, and ops/admin platform
- support the defined in-scope service types
- provide transparent pricing before booking
- provide assignment and reassignment workflows
- support live tracking and customer-confirmed trip start
- support digital payment, invoicing, and refund handling
- support safety, support, and incident reporting
- provide operational tooling for onboarding, dispatch rescue, and service management

### 6.2 Non-Goals

- multi-city expansion in MVP
- owned fleet operations
- rental car inventory
- complex corporate tenancy
- subscription-based economy in MVP
- AI-based pricing or AI-dependent dispatch
- cashback, loyalty, or gamified retention systems
- fully automated operations with no human intervention

## 7. Scope Summary

### 7.1 In-Scope Service Types

- Scheduled Local
- Scheduled One-Way Drop
- Scheduled Round Trip
- Airport Pickup/Drop
- Late-Night Safe Return

### 7.2 Out-of-Scope Services

- self-drive rentals
- rental car with driver
- outstation multi-day service
- wedding chauffeur vertical
- employee shuttle products
- white-labeled B2B transport portal

### 7.3 Launch Market Scope

- one city only
- 2-3 dense micro-zones
- geographically controlled serviceability

## 8. Target Users

### 8.1 Customer Personas

#### Persona A: Urban Professional

- owns a car
- books airport, meeting, and evening return rides
- values punctuality and low-friction booking
- likely to become a repeat user if the experience is predictable

#### Persona B: Family Coordinator

- books for family members, parents, spouse, or guests
- values trust, safety, and live share
- wants to know exactly who is driving and when they will arrive

#### Persona C: Reliability-Seeking Repeat User

- uses the service for frequent local travel
- prefers planning over uncertainty
- is highly sensitive to delays and hidden charges

### 8.2 Driver Persona

- independent licensed driver
- wants clarity on earnings before accepting a job
- prefers planned work rather than high-chaos on-demand supply
- needs clear support, dispute handling, and onboarding status

### 8.3 Internal Users

- ops executive
- city operations manager
- support specialist
- driver onboarding reviewer
- finance/admin reviewer

## 9. Assumptions and Constraints

The following assumptions are used in this PRD to avoid ambiguity.

### 9.1 Business Assumptions

- launch remains single-city
- launch remains scheduled-first
- launch remains private-car-owner focused
- ops remains in the loop for rescue, refunds, and incidents

### 9.2 Product Assumptions

- customer authentication will be mobile OTP based
- driver authentication will be mobile OTP based
- at least one digital payment method is required at launch
- the default launch digital payment method is UPI
- push notifications and SMS are the required communication channels at launch
- WhatsApp is not required for MVP

### 9.3 Technology Assumptions

- backend will be a Spring Boot modular monolith
- customer and driver apps will be built in React Native
- admin/ops will be delivered as a web app
- PostgreSQL will be the source of truth for transactional data

### 9.4 Operational Constraints

- driver onboarding will include manual approval
- assignment failures must be recoverable via human intervention
- support processes must exist before launch for refund and incident handling

## 10. Dependencies

### 10.1 External Dependencies

- SMS OTP provider
- push notification provider
- maps and geocoding provider
- payment gateway with UPI support
- object storage for driver documents and evidence

### 10.2 Internal Dependencies

- city and zone serviceability configuration
- pricing and payout policy approval
- driver onboarding SOP
- refund and incident SOP
- launch support ownership

## 11. Success Metrics

The following are proposed launch metrics and guardrails. These targets should be reviewed after the first meaningful operating sample, but they are the starting success criteria for the MVP.

| Metric | Definition | Initial Target | Guardrail / Red Flag |
|---|---|---:|---:|
| Activation Rate | % of signed-up customers who request at least one quote within 7 days | >= 60% | < 40% |
| Quote to Booking Conversion | % of quotes that become confirmed bookings | >= 25% | < 15% |
| Fulfillment Rate | % of confirmed bookings that get a driver and proceed to trip start | >= 90% | < 80% |
| On-Time Arrival Rate | % of assigned rides where driver arrives within SLA | >= 85% | < 70% |
| Trip Completion Rate | % of started trips that complete successfully | >= 97% | < 93% |
| 30-Day Repeat Rate | % of first-booking customers who book again within 30 days | >= 25% | < 15% |
| Support Ticket Rate | Tickets per 100 completed trips | <= 10 | > 20 |
| Refund Rate | % of completed trips requiring refund or major adjustment | <= 5% | > 10% |
| Driver Acceptance Rate | % of dispatched offers accepted by eligible drivers | >= 60% | < 45% |
| Driver Cancellation Rate | % of accepted assignments later cancelled by drivers | <= 8% | > 15% |
| Contribution Margin Trend | Average contribution margin per completed trip | non-negative by pilot stabilization | negative after stabilization |
| Customer Rating | Average post-trip customer rating | >= 4.5 / 5 | < 4.0 / 5 |

## 12. Launch Readiness Requirements

The MVP must not go live unless all of the following are true:

- all P0 product requirements are implemented
- all critical customer, driver, and ops journeys pass UAT
- pricing logic is approved by business and finance
- driver payout logic is approved by business and finance
- driver onboarding SOP exists and is operationalized
- cancellation and refund SOP exists
- support and incident severity workflow exists
- launch city and zone serviceability are configured
- production monitoring and alerting are active

## 13. Priority Framework

This PRD uses the following priority language:

- `P0`: mandatory for MVP launch
- `P1`: important for MVP but launch may proceed with controlled workaround
- `P2`: explicitly deferred beyond MVP

All user stories and requirements in the main functional epics below are considered `P0` unless otherwise marked.

## 14. User Journey Overview

### 14.1 Customer Journey

1. customer signs in with mobile OTP
2. customer selects service type
3. customer enters pickup, drop, date, and time
4. system validates serviceability
5. system returns quote with full fare breakdown
6. customer confirms booking
7. system starts assignment workflow
8. driver is assigned or booking escalates for rescue
9. customer receives driver details and ETA
10. driver arrives and marks arrival
11. customer confirms trip start
12. trip proceeds with live tracking and support access
13. trip completes
14. invoice and payment are handled
15. customer rates driver or reports issue

### 14.2 Driver Journey

1. driver signs in with OTP
2. driver completes onboarding profile and uploads documents
3. ops approves driver
4. driver becomes eligible for assignments
5. driver receives assignment with earning preview
6. driver accepts
7. driver navigates to pickup and marks arrived
8. driver waits for customer trip-start confirmation
9. driver completes trip
10. driver sees earnings summary

### 14.3 Ops Journey

1. ops monitors booking queue
2. ops sees unassigned or at-risk bookings
3. ops intervenes with reassignment if necessary
4. ops manages onboarding approvals
5. ops handles incidents, support, and refunds
6. ops tracks service health and performance dashboards

## 15. Functional Requirements by Epic

## 15.1 Epic A: Customer Identity and Account Basics

### Objective

Allow customers to sign in quickly, establish a valid account, and become ready to request quotes and create bookings with minimal friction.

### Business Requirements

- `BR-A1` The system shall allow customer sign-up and sign-in using mobile OTP.
- `BR-A2` The system shall prevent account creation or session completion without successful OTP verification.
- `BR-A3` The system shall collect and store the customer's name and selected city on first successful login.
- `BR-A4` The system shall record customer consent for location and communication where applicable.
- `BR-A5` The system shall support returning-customer login without re-entering profile data unless required by policy.
- `BR-A6` The system shall support logout and session invalidation.

### User Stories

#### US-A1

As a first-time customer, I want to log in using my phone number and OTP so that I can start using the service without a long registration form.

#### US-A2

As a returning customer, I want to sign in quickly and land on a ready-to-book home screen so that I can make repeat bookings with minimal effort.

#### US-A3

As the business, I want only verified mobile numbers to create active customer accounts so that fraud and fake accounts are reduced.

### Edge Cases

- invalid phone number format
- OTP not received
- OTP expired
- OTP entered incorrectly multiple times
- duplicate OTP requests in a short interval
- user closes app during verification
- city not yet supported

### Acceptance Criteria

- Given a valid mobile number, when a customer requests OTP, then the system sends an OTP and shows an OTP entry screen.
- Given a valid OTP, when the customer verifies it successfully, then the system creates or resumes the customer account and starts an authenticated session.
- Given an invalid or expired OTP, when the customer submits it, then the system shows a clear retry path and does not create an active session.
- Given a first-time login, when OTP verification succeeds, then the system requires name and city before the booking home screen is shown.
- Given an unsupported city, when the user selects it, then the system shall block booking access and communicate that the city is not live yet.

## 15.2 Epic B: Service Discovery, Quote, and Pricing Transparency

### Objective

Enable customers to understand service availability and total expected pricing before booking, with no hidden components.

### Business Requirements

- `BR-B1` The system shall support quote generation for all in-scope service types.
- `BR-B2` The system shall validate pickup, drop, schedule, and service type against serviceability rules before returning a quote.
- `BR-B3` The system shall return a quote with itemized pricing components.
- `BR-B4` The system shall show quote expiry and enforce quote validity windows.
- `BR-B5` The system shall store a quote snapshot when a booking is created.
- `BR-B6` The system shall explain service unavailability when a quote cannot be generated.
- `BR-B7` The pricing model shall support city, zone, lead time, day/time window, night band, and service type inputs.
- `BR-B8` For Bengaluru launch, the pricing model shall support hybrid distance-time inputs so one-way and round-trip fares reflect traffic reality instead of distance alone.
- `BR-B9` For one-way trips, the quote shall display base fare, distance fee, traffic-time fee, driver pickup access fee, one-way relocation allowance, optional safety fee, taxes, and any peak/night charge separately.
- `BR-B10` For round trips, the quote shall display bundled base fare, included distance/time, any extra distance/time fee, single pickup access fee, bundle savings explanation, taxes, and any peak/night charge separately.
- `BR-B11` The quote response shall include a driver payout preview for internal/debug/admin visibility and future driver-facing offer transparency.
- `BR-B12` The system shall avoid opaque surge pricing in MVP; peak pricing must be a visible capped traffic risk fee.

### User Stories

#### US-B1

As a customer, I want to see a complete fare breakdown before I confirm a booking so that I do not face billing surprises later.

#### US-B2

As a customer, I want to know immediately if my route or time is not serviceable so that I do not waste time trying to book something impossible.

#### US-B3

As ops/admin, I want the pricing rules to be configurable by city and zone so that the business can operate without code changes for every pricing update.

### Edge Cases

- pickup outside serviceable zone
- airport service requested from unsupported area
- quote requested for a past time
- one-way service requested without drop location
- distance or traffic ETA unavailable from maps provider
- driver pickup acquisition estimate unavailable
- predicted pickup ETA above the standard 30-minute SLA
- route distance is fractional and must be rounded up
- automatic/luxury/SUV vehicle requires skill or payout adjustment
- quote expires before booking confirmation
- multiple rapid quote refreshes
- location permission denied

### Acceptance Criteria

- Given a serviceable request, when the customer requests a quote, then the system returns the quote within the target latency and includes all active fare components, taxes, policy notes, pricing assumptions, and quote expiry.
- Given a Bengaluru one-way quote, when distance, time, and pickup acquisition inputs are present, then the fare shall be calculated using the hybrid one-way formula and the response shall show distance, traffic time, pickup access, relocation, safety, and tax components separately.
- Given a Bengaluru round-trip quote, when distance and time inputs are present, then the fare shall be calculated using the discounted round-trip bundle formula and shall not charge pickup acquisition twice.
- Given a non-serviceable request, when the customer requests a quote, then the system returns no quote and provides a clear reason.
- Given a quote with an expiry window, when the customer confirms within the validity window, then the booking shall use the quote snapshot without recalculating hidden charges.
- Given an expired quote, when the customer attempts to confirm, then the system shall require a fresh quote before booking.
- Given a night-time service, when the customer views the quote, then the night surcharge shall be displayed explicitly rather than silently added later.
- Given peak-hour Bengaluru conditions, when a capped traffic risk fee applies, then the customer shall see it as an explicit line item and the driver payout preview shall include the corresponding peak bonus.

## 15.3 Epic C: Booking Creation, Modification, and Cancellation

### Objective

Allow customers to confirm bookings confidently, manage them before trip start, and cancel with clear policy visibility.

### Business Requirements

- `BR-C1` The system shall allow a customer to create a booking from a valid quote.
- `BR-C2` The system shall create bookings in a trackable lifecycle state model.
- `BR-C3` The system shall allow customers to view upcoming and past bookings.
- `BR-C4` The system shall allow cancellation before trip completion subject to policy.
- `BR-C5` The system shall show the applicable cancellation outcome before the customer confirms cancellation.
- `BR-C6` The system shall support controlled booking modification for supported fields before dispatch lock thresholds.
- `BR-C7` The system shall protect against duplicate booking creation caused by repeated taps or retries.

### User Stories

#### US-C1

As a customer, I want to confirm a booking from a valid quote so that I know the ride request has been accepted by the platform.

#### US-C2

As a customer, I want to see all upcoming bookings so that I can plan my day and verify that the service is still scheduled.

#### US-C3

As a customer, I want to understand the cancellation fee before I cancel so that I can make an informed decision.

#### US-C4

As the business, I want duplicate booking prevention so that customer mistakes and billing confusion are minimized.

### Edge Cases

- user double taps confirm booking
- booking request retried on poor network
- user modifies booking after assignment has begun
- user cancels just after driver is assigned
- user cancels after driver has arrived
- user attempts cancellation after trip has already started

### Acceptance Criteria

- Given a valid quote, when the customer confirms the booking once, then exactly one booking record shall be created.
- Given a confirmed booking, when the customer opens the booking detail screen, then the system shall show booking status, schedule, fare snapshot, and assignment status.
- Given a booking in a cancellable state, when the customer taps cancel, then the system shall display the expected fee or no-fee outcome before final confirmation.
- Given a booking in a non-cancellable state such as completed, when the customer tries to cancel, then the system shall block the action and offer support or issue reporting instead.
- Given a booking modification request outside the allowed modification window, when the customer tries to modify, then the system shall either block the action or route the customer to support as per policy.

## 15.4 Epic D: Driver Assignment, Backup Fulfillment, and Rescue

### Objective

Assign eligible drivers reliably, recover quickly when assignment risk appears, and keep the customer informed without exposing operational chaos.

### Business Requirements

- `BR-D1` The system shall initiate driver assignment automatically after booking confirmation.
- `BR-D2` The system shall filter driver candidates based on zone, availability, compliance, and service eligibility.
- `BR-D3` The system shall record assignment attempts and final assignment state.
- `BR-D4` The system shall support reassignment if the original assignment becomes at risk or fails.
- `BR-D5` The system shall expose assignment rescue tasks to ops when automated assignment does not succeed in time.
- `BR-D6` The system shall notify the customer when a driver is assigned and when ETA materially changes.
- `BR-D7` The system shall allow ops to manually reassign under audit.

### User Stories

#### US-D1

As a customer, I want a driver to be assigned reliably after booking so that I trust the platform to fulfill what I paid for.

#### US-D2

As ops, I want to see unassigned and at-risk bookings in one queue so that I can rescue rides before customers experience failure.

#### US-D3

As the business, I want reassignment to be a first-class capability so that one driver failure does not automatically become a customer failure.

### Edge Cases

- no eligible drivers for the requested time
- assigned driver stops responding
- assigned driver cancels close to pickup
- driver ETA worsens significantly
- ops manually overrides assignment during active rescue
- driver document expires between offer and trip

### Acceptance Criteria

- Given a new confirmed booking, when assignment starts, then the system shall only consider drivers who satisfy eligibility rules.
- Given a successful driver acceptance, when the assignment is finalized, then the customer shall see driver name, photo, rating, language if available, and ETA.
- Given no successful assignment within configured thresholds, when the booking enters risk state, then an ops rescue task shall be created automatically.
- Given a reassignment event, when a new driver is locked, then the customer shall see the updated driver details and not see stale assignment data.
- Given an admin or ops reassignment, when it is executed, then the action shall be logged with actor, time, and reason.

## 15.5 Epic E: Trip Start, Tracking, and Completion

### Objective

Ensure that ride execution is trustworthy from arrival to completion, with customer-confirmed start, live visibility, and clean end-of-trip summary.

### Business Requirements

- `BR-E1` The driver shall be able to mark arrival at pickup.
- `BR-E2` The customer shall receive an arrival notification and see a trip-start confirmation screen.
- `BR-E3` Billing shall not begin until customer trip-start confirmation or an audited ops exception.
- `BR-E4` The system shall support an optional pre-trip handover checklist.
- `BR-E5` The system shall provide live tracking during driver approach and in-trip movement.
- `BR-E6` The customer shall be able to share live trip status.
- `BR-E7` The system shall support trip completion and final fare calculation.
- `BR-E8` The system shall preserve trip events for audit and support.

### User Stories

#### US-E1

As a customer, I want to confirm that the driver has actually arrived and start the trip myself so that no early billing can happen without my knowledge.

#### US-E2

As a customer, I want to track the driver and the ride live so that I feel safe and informed.

#### US-E3

As the business, I want every trip to have an auditable event trail so that disputes can be investigated fairly.

### Edge Cases

- customer cannot start the trip due to poor network
- driver arrives but at the wrong location
- customer phone battery is dead at pickup
- tracking pings stop temporarily
- destination changes mid-trip
- driver tries to complete trip before customer expects it to end

### Acceptance Criteria

- Given a driver has marked arrived, when the customer opens the booking, then the trip-start screen shall be available.
- Given arrival has been marked, when the customer has not yet confirmed start, then the trip state shall not move to `IN_PROGRESS` and billing shall not begin.
- Given the customer confirms trip start, when confirmation succeeds, then the trip state shall move to `IN_PROGRESS` and the timeline shall record the start event.
- Given the trip is active, when the customer opens the trip screen, then live tracking, support, and share-trip controls shall be visible.
- Given the trip is completed, when the driver and system finalize the trip, then the customer shall see a summary with start time, end time, duration, fare, and next actions.

## 15.6 Epic F: Payments, Invoices, and Refunds

### Objective

Collect payment cleanly, generate understandable invoices, and support controlled refund handling.

### Business Requirements

- `BR-F1` The MVP shall support at least one digital payment method, with UPI required for launch.
- `BR-F2` The system shall present the payable amount and fare breakdown before payment confirmation.
- `BR-F3` The system shall generate an invoice for completed trips.
- `BR-F4` The system shall record payment success, failure, and pending states.
- `BR-F5` The system shall support refund initiation by authorized internal users.
- `BR-F6` The system shall require refund reason capture and audit logging.
- `BR-F7` The system shall prevent silent post-trip charges that were never disclosed in the pricing model.

### User Stories

#### US-F1

As a customer, I want to pay digitally after my ride and receive a clear invoice so that I trust the final bill.

#### US-F2

As support or finance, I want to issue refunds with reason codes so that customer resolution is consistent and traceable.

#### US-F3

As the business, I want payment and refund events recorded correctly so that finance reconciliation is possible.

### Edge Cases

- payment fails after trip completion
- payment is interrupted by network loss
- duplicate payment attempt
- customer disputes additional charges
- partial refund required
- refund requested for already refunded trip

### Acceptance Criteria

- Given a completed trip, when the customer reaches payment, then the payable amount shall match the final approved fare breakdown.
- Given successful payment, when the payment provider callback is received, then the invoice and trip record shall both reflect paid status.
- Given payment failure, when the payment callback or timeout indicates failure, then the trip shall remain unpaid and supportable without losing invoice context.
- Given an authorized refund request, when support or finance submits it, then the system shall record the amount, reason, actor, and timestamp.
- Given a fare component was not part of the original approved pricing model and not added via an explicit auditable flow, then it shall not appear as a hidden final charge.

## 15.7 Epic G: Driver Onboarding and Compliance

### Objective

Ensure only eligible, verified, and operationally approved drivers can receive assignments.

### Business Requirements

- `BR-G1` The system shall support driver OTP login and profile creation.
- `BR-G2` The system shall support required document upload for onboarding.
- `BR-G3` The system shall place new drivers into an approval workflow before assignment eligibility.
- `BR-G4` The system shall allow ops to approve, reject, or request correction on driver onboarding.
- `BR-G5` The system shall prevent unapproved or non-compliant drivers from receiving assignments.
- `BR-G6` The system shall retain onboarding status and audit history.

### User Stories

#### US-G1

As a prospective driver, I want to upload my required documents and track my onboarding status so that I know what is preventing me from going live.

#### US-G2

As ops, I want to review and approve drivers before they receive bookings so that service quality and safety standards are controlled.

#### US-G3

As the business, I want compliance gating on assignment eligibility so that expired or missing documents do not create operational risk.

### Edge Cases

- incomplete document upload
- blurred or unreadable document image
- rejected application with correction request
- driver tries to go online before approval
- driver documents expire after approval

### Acceptance Criteria

- Given a new driver account, when required profile and document fields are incomplete, then the system shall show onboarding as incomplete and block assignment eligibility.
- Given all required documents are uploaded, when ops approves the driver, then the driver shall become eligible for availability and job assignment.
- Given the driver is rejected or sent back for correction, when the driver logs in, then the app shall clearly show the reason and next step.
- Given a driver document is expired or suspended, when dispatch evaluates candidates, then the driver shall be ineligible for assignment.

## 15.8 Epic H: Driver Availability, Job Execution, and Earnings Transparency

### Objective

Give drivers clear control over availability, ride acceptance, and earnings visibility so the platform is trustworthy for supply-side participation.

### Business Requirements

- `BR-H1` The system shall allow approved drivers to control availability for work.
- `BR-H2` The system shall show core ride details and earning preview before the driver accepts an assignment.
- `BR-H3` The system shall allow drivers to accept or decline assignments.
- `BR-H4` The system shall support driver arrival and trip completion updates.
- `BR-H5` The system shall show drivers a breakdown of earnings for completed trips.
- `BR-H6` The system shall retain driver earning ledgers for audit and payout processing.

### User Stories

#### US-H1

As a driver, I want to see how much I am expected to earn before I accept a trip so that I can make informed decisions.

#### US-H2

As a driver, I want to control when I am available so that I can manage my working hours.

#### US-H3

As a driver, I want a clear completed-trip earnings summary so that I trust the payout system.

### Edge Cases

- driver accepts while going offline
- driver misses the assignment response window
- driver loses connectivity after acceptance
- driver reaches pickup but customer is unreachable
- completed-trip earnings do not match preview due to allowed adjustments

### Acceptance Criteria

- Given an eligible assignment offer, when the driver views it, then the app shall show pickup area, service type, schedule, and earning preview.
- Given the driver accepts a job, when the acceptance is confirmed, then the assignment shall be locked or updated according to dispatch policy.
- Given the driver marks arrived, when customer start confirmation is pending, then the driver app shall show that start is awaiting customer confirmation.
- Given a completed trip, when the driver opens the result screen, then the app shall show earning components including arrival fee, active-time payout, night bonus if any, and any tip if applicable.

## 15.9 Epic I: Safety, Support, and Incident Management

### Objective

Provide strong trust and safety features for active rides and create structured support and incident workflows for resolution.

### Business Requirements

- `BR-I1` The customer app shall expose SOS and support access during active trips.
- `BR-I2` The app shall support live trip sharing.
- `BR-I3` The app shall support masked communication between customer and driver where applicable.
- `BR-I4` The system shall support support-ticket creation from booking or trip context.
- `BR-I5` The system shall support incident severity classification and evidence capture.
- `BR-I6` The system shall create an auditable record of actions taken on incidents and support tickets.
- `BR-I7` The system shall support clear post-trip issue reporting categories.

### User Stories

#### US-I1

As a customer, I want an SOS button and live trip sharing so that I feel safe during the ride.

#### US-I2

As a customer, I want to report a billing issue or behavior issue with the ride context already attached so that I do not have to explain everything from scratch.

#### US-I3

As support, I want structured categories and evidence on cases so that I can resolve tickets consistently and quickly.

### Edge Cases

- customer raises issue after trip completion
- customer raises safety issue during trip
- customer has no data but needs emergency help
- driver raises issue against customer
- support case reopened after closure

### Acceptance Criteria

- Given an active trip, when the customer opens the trip screen, then SOS, support, and share-trip actions shall be available.
- Given a customer creates a support ticket from a booking or trip, when the ticket is submitted, then booking or trip context shall be attached automatically.
- Given an incident is raised, when support or ops triages it, then severity, owner, and current status shall be recorded.
- Given an authorized support action such as refund, note, or escalation, when it is applied, then the case shall record the action with timestamp and actor.

## 15.10 Epic J: Operations and Admin Platform

### Objective

Enable ops and admins to run the business day to day, including onboarding, assignment rescue, refunds, pricing, and serviceability.

### Business Requirements

- `BR-J1` The system shall provide a booking operations queue with booking state visibility.
- `BR-J2` The system shall provide at-risk and unassigned booking rescue visibility.
- `BR-J3` The system shall support manual assignment and reassignment by authorized ops users.
- `BR-J4` The system shall support driver onboarding review and approval.
- `BR-J5` The system shall support refund workflows and pricing overrides under audit.
- `BR-J6` The system shall support city and zone serviceability configuration.
- `BR-J7` The system shall enforce role-based access control on admin actions.
- `BR-J8` The system shall preserve audit logs for privileged actions.

### User Stories

#### US-J1

As ops, I want one place to monitor booking health so that I can rescue rides before they fail.

#### US-J2

As ops, I want to approve drivers and manage serviceability without waiting for engineering so that the business can move quickly.

#### US-J3

As finance/support, I want refund and override actions to be logged so that there is accountability and traceability.

### Edge Cases

- two ops users attempt conflicting reassignment actions
- refund and override actions happen after shift change
- pricing config update affects unsupported zones
- admin without correct role attempts privileged action

### Acceptance Criteria

- Given authorized ops access, when an ops user opens the bookings console, then they shall see current booking state and assignment health.
- Given a booking is at risk, when the rescue threshold is crossed, then the booking shall appear in the at-risk queue.
- Given a user without permission attempts a privileged action such as refund or pricing override, when the request is made, then the system shall deny the action.
- Given a privileged admin action is completed, when the action succeeds, then the system shall record it in the audit log.

## 15.11 Epic K: Notifications and Communication

### Objective

Keep customers and drivers informed at each meaningful step without contradictory or duplicated communication.

### Business Requirements

- `BR-K1` The system shall send notifications for OTP, booking confirmation, assignment, driver arrival, trip start, trip completion, payment status, and refund status.
- `BR-K2` The system shall support push notifications and SMS for critical lifecycle events.
- `BR-K3` Notification delivery shall be idempotent to avoid duplicate messaging from repeated events or callbacks.
- `BR-K4` Customer-facing notifications shall reflect the latest known trip and assignment state.

### User Stories

#### US-K1

As a customer, I want timely notifications when the booking status changes so that I do not have to keep checking the app constantly.

#### US-K2

As a driver, I want clear assignment and trip notifications so that I can respond quickly and reliably.

#### US-K3

As the business, I want notification consistency so that customers do not receive confusing or contradictory updates.

### Edge Cases

- duplicate provider callbacks
- push notification fails
- customer app is offline
- driver device is offline at assignment time
- reassignment occurs after initial driver assignment notification

### Acceptance Criteria

- Given a booking confirmation, when the booking is successfully created, then the customer shall receive a confirmation notification.
- Given a driver is assigned, when the assignment is finalized, then the customer shall receive an assignment notification with the current driver context.
- Given a critical push notification fails or is unavailable, when fallback is configured for that event, then SMS shall be used for the critical update.
- Given reassignment occurs, when the new driver is locked, then subsequent notifications shall reference the new driver and not the stale driver.

## 15.12 Epic L: Reporting, Audit, and Observability

### Objective

Ensure that the business can monitor product health, investigate problems, and make informed decisions from day one.

### Business Requirements

- `BR-L1` The system shall record audit events for privileged operational actions.
- `BR-L2` The system shall expose core business dashboards for booking funnel, supply health, support, and margins.
- `BR-L3` The system shall support technical monitoring for latency, failures, and stuck-state anomalies.
- `BR-L4` The system shall retain sufficient booking, assignment, trip, and support history for analysis and resolution.
- `BR-L5` The system shall provide alerts for major operational failures such as payment callback issues, assignment backlog, and tracking failure.

### User Stories

#### US-L1

As a city manager, I want to see fulfillment, ticket, and margin health so that I can detect operational problems early.

#### US-L2

As engineering, I want monitoring and alerting for critical workflows so that production issues are detected quickly.

#### US-L3

As support and compliance, I want audit trails for operational changes so that disputes and escalations can be investigated fairly.

### Edge Cases

- dashboard data lag
- monitoring not firing for a failed background job
- missing event in a trip timeline
- audit trail access without proper permissions

### Acceptance Criteria

- Given privileged operational actions such as reassignment, refund, or pricing override, when these actions occur, then audit logs shall capture actor, timestamp, action, and reason where applicable.
- Given production traffic, when critical booking or payment flows degrade beyond thresholds, then the system shall raise alerts.
- Given a manager opens the operational dashboard, when data is available, then core KPIs shall include fulfillment, assignment time, refund rate, and support volume.

## 16. Non-Functional Requirements

### 16.1 Availability

- `NFR-1` Customer booking APIs shall target 99.9% monthly availability.
- `NFR-2` Live trip state and payment workflows shall be treated as higher criticality than low-priority reporting workflows.

### 16.2 Performance

- `NFR-3` Quote generation shall target p95 response time of under 2 seconds.
- `NFR-4` Assignment confirmation workflows shall target timely response within the configured operating SLA.
- `NFR-5` Customer trip tracking updates shall refresh at a usable interval appropriate to battery and network conditions.

### 16.3 Security

- `NFR-6` Sensitive personal data shall be protected in transit and at rest using appropriate encryption controls.
- `NFR-7` Admin and ops access shall be role-based and auditable.
- `NFR-8` OTP abuse and brute-force patterns shall be rate-limited.

### 16.4 Reliability

- `NFR-9` Booking creation shall be idempotent against duplicate client submissions.
- `NFR-10` Payment callback processing shall be idempotent.
- `NFR-11` Event-driven background jobs shall be monitored for failure and retries.

### 16.5 Maintainability

- `NFR-12` The backend shall follow modular monolith boundaries aligned with the HLD.
- `NFR-13` Configuration-driven pricing and serviceability shall be supported without requiring code changes for routine operational updates.

### 16.6 Compliance and Audit

- `NFR-14` GST-compliant invoice support is required.
- `NFR-15` Critical operational actions shall be auditable.
- `NFR-16` Data retention policies for trip, support, KYC, and finance data shall be defined before production launch.

## 17. Edge Case Catalogue

This section collects launch-critical edge cases that must be handled either in product flow or via documented ops fallback.

### 17.1 Identity and Access Edge Cases

- OTP not delivered
- OTP expired
- wrong OTP entered multiple times
- unsupported city selection
- user changes device during login flow

### 17.2 Quote and Booking Edge Cases

- serviceability failure
- quote expires before booking
- duplicate booking submission
- booking created but assignment not started due to background failure
- customer schedules a ride in the past

### 17.3 Assignment Edge Cases

- no drivers available
- driver accepts then cancels
- driver does not move toward pickup
- driver device goes offline after acceptance
- ops and system both attempt rescue at once

### 17.4 Trip Execution Edge Cases

- customer cannot confirm start due to poor connectivity
- customer phone battery dies at pickup
- wrong driver arrives
- GPS accuracy is poor
- trip start attempted before actual arrival
- trip completion attempted too early

### 17.5 Payment Edge Cases

- payment interrupted
- callback delayed
- duplicate callback
- refund already processed
- customer disputes final amount

### 17.6 Support and Safety Edge Cases

- emergency raised during no-network condition
- post-trip severe misconduct claim
- customer and driver provide conflicting accounts
- evidence uploaded after ticket closure

### 17.7 Driver Operations Edge Cases

- driver approval reversed after live assignment eligibility
- driver compliance expires mid-day
- payout discrepancy claim
- driver disputes cancellation penalty

## 18. Global Acceptance Criteria for MVP

The MVP shall be accepted only if all of the following conditions are met:

- a first-time customer can sign in, request a quote, book, start, complete, pay, and rate a ride without support intervention in the happy path
- a returning customer can rebook successfully from the home flow
- a driver can onboard, be approved, accept a job, mark arrived, wait for customer start confirmation, complete the trip, and see earnings
- ops can detect and rescue at-risk assignments
- support can open, triage, and resolve the primary ticket categories
- pricing appears clearly before booking and remains auditable after trip completion
- no hidden charge can be inserted outside approved or audited flows
- trip-start confirmation prevents early billing by default
- notifications, payments, and tracking function reliably enough for launch operations

## 19. Out-of-Scope Items for MVP

The following are intentionally deferred and shall not be added to MVP unless scope is formally reopened:

- subscriptions
- prepaid ride credits
- preferred drivers
- family account hierarchy
- recurring commute automation
- corporate dashboards
- corporate billing and invoicing
- wallet
- cashback or coupon platform
- loyalty points
- AI dispatch or AI pricing
- chatbot support
- outstation multi-day service
- rental-car supply
- multi-city support

## 20. Open Decisions Requiring Resolution Before Detailed Design Lock

These are not blockers to this PRD, but they must be finalized before implementation reaches production readiness.

### 20.1 Commercial Decisions

- exact launch city
- exact launch zones
- final price values by service type
- exact night surcharge values
- exact cancellation fee thresholds
- exact payout cadence to drivers

### 20.2 Operational Decisions

- launch support hours
- launch operating hours by zone
- whether cash payment is allowed as fallback
- exact document verification vendor or manual process mix

### 20.3 Legal and Compliance Decisions

- document retention periods
- support record retention periods
- privacy policy and terms wording
- incident escalation obligations by category

## 21. Risks

### 21.1 Product Risks

- too much friction in booking reduces conversion
- too little transparency reduces trust
- over-complex pricing weakens usability

### 21.2 Operational Risks

- supply shortages cause fulfillment failures
- slow rescue handling hurts brand trust
- inconsistent refunds create customer dissatisfaction

### 21.3 Financial Risks

- underpriced rides damage unit economics
- overcompensation and high refunds compress margins
- payment failures increase support load

### 21.4 Technical Risks

- duplicate bookings from retry flows
- tracking inconsistency during low-network conditions
- missing audit trails for sensitive actions

## 22. Release Recommendations

To stay aligned with the low-burn strategy, the first release should:

- launch only with the minimum operating zones needed for reliable coverage
- prioritize scheduled bookings over instant demand
- prioritize UPI and clean invoicing over broad payment-method expansion
- use human-led ops rescue rather than overbuilding automation
- validate repeat behavior before adding any deferred growth or loyalty features

## 23. Summary

This PRD defines the Rydvrse MVP as a focused, trust-first, operationally realistic product.

The core promise is simple:

`book a verified driver for your own car with clear pricing, reliable assignment, controlled trip start, and strong support when needed.`

If the team builds and validates against this PRD, Rydvrse will launch with a coherent product shape, measurable success criteria, and clear acceptance boundaries.

# Rydvrse Implementation Backlog

## Document Control

- Document Name: `Implementation_Backlog.md`
- Product: `Rydvrse`
- Version: `1.0`
- Status: `Execution Backlog Baseline for MVP`
- Last Updated: `2026-04-10`
- Source Documents:
  - `MVP_Scope.md`
  - `Product_Requirements_Document.md`
  - `Screen_Flow.md`
  - `Wireframes_and_UX_States.md`
  - `Pricing_and_Payout_Design.md`
  - `Database_Schema.md`
  - `API_Spec.md`
  - `Low_Level_Design.md`
  - `Infrastructure_and_Delivery_Plan.md`
  - `Ops_SOP.md`
- Intended Audience:
  - founders
  - engineering
  - product and design
  - ops and support
  - QA and release owners

## 1. Purpose

This document converts the Rydvrse design set into an execution backlog.

It defines:

- workstreams
- owners
- epics
- milestones
- sprint sequencing
- task dependencies
- release gates

This is the point where planning becomes delivery.

## 2. Planning Assumptions

To remove ambiguity, this backlog assumes:

- MVP delivery is planned in `8 implementation sprints`
- sprint length is `2 weeks`
- there is a short `Sprint 0` for setup and planning lock
- owners are listed by role, not by person name
- the team is lean, so some owners may wear multiple hats

If the actual team is smaller:

- extend the timeline
- do not reopen MVP scope casually
- do not add deferred features to compensate for velocity pressure

## 3. Team Model and Workstream Owners

### 3.1 Role-Based Owners

| Role | Primary Scope |
|---|---|
| `Founder / Product Owner` | scope control, decision-making, acceptance |
| `Engineering Lead` | architecture, sequencing, technical sign-off |
| `Backend Lead` | Spring Boot platform, APIs, domain modules |
| `Mobile Lead` | customer and driver React Native apps |
| `Admin Web Lead` | ops/admin console |
| `Platform / DevOps Lead` | environments, CI/CD, observability, secrets |
| `QA Lead` | test planning, regression, UAT support |
| `Ops Lead` | rescue workflows, support readiness, launch operations |
| `Support Lead` | support process, refund decision quality, incident handling |
| `Finance / Compliance Owner` | refunds, invoice checks, payout and audit validation |

### 3.2 Workstream Ownership

| Workstream | Primary Owner | Supporting Roles |
|---|---|---|
| Product and scope governance | `Founder / Product Owner` | `Engineering Lead`, `Ops Lead` |
| Backend platform | `Backend Lead` | `Engineering Lead`, `Platform / DevOps Lead` |
| Customer mobile | `Mobile Lead` | `Backend Lead`, `QA Lead` |
| Driver mobile | `Mobile Lead` | `Backend Lead`, `Ops Lead` |
| Admin and ops web | `Admin Web Lead` | `Backend Lead`, `Ops Lead` |
| Infrastructure and CI/CD | `Platform / DevOps Lead` | `Engineering Lead` |
| Ops readiness | `Ops Lead` | `Support Lead`, `Finance / Compliance Owner` |
| QA and release | `QA Lead` | all leads |

## 4. Definition of Done

A backlog item is not done unless:

- implementation is complete
- tests for the change are added or updated where applicable
- acceptance criteria are verified
- logs, metrics, or audit behavior are added for critical flows when required
- documentation is updated if the implementation changed a locked design decision
- QA or owner sign-off is recorded for milestone-critical items

## 5. Delivery Strategy

### 5.1 Critical Path

The MVP critical path is:

1. platform and infra foundation
2. auth and base identities
3. pricing and quote
4. booking creation
5. dispatch and assignment
6. trip start and completion
7. payment and invoice
8. support and refund handling
9. end-to-end hardening
10. launch readiness

### 5.2 Parallel Work

The following can progress in parallel once dependencies are available:

- customer and driver app scaffolding
- admin web scaffolding
- ops SOP operationalization
- observability dashboards
- QA test-case writing

## 6. Milestone Plan

### 6.1 Milestone Overview

| Milestone | Target Sprint | Outcome | Primary Owner |
|---|---|---|---|
| `M0` Planning Lock | `Sprint 0` | execution-ready repo, owners, environments plan, final design lock | `Founder / Product Owner` |
| `M1` Platform Foundation | `Sprint 1` | backend skeleton, CI, migrations, auth baseline | `Engineering Lead` |
| `M2` Identity and Core Profiles | `Sprint 2` | customer and driver base onboarding flows working | `Backend Lead` |
| `M3` Commercial and Booking Happy Path | `Sprint 3-4` | quote, booking create, booking detail, base customer flow | `Backend Lead` |
| `M4` Dispatch and Driver Execution | `Sprint 5` | assignment, driver availability, offer accept, rescue queue | `Ops Lead` |
| `M5` Trip, Tracking, and Notifications | `Sprint 6` | arrival, start confirmation, live tracking, notifications | `Mobile Lead` |
| `M6` Payments, Support, and Admin Controls | `Sprint 7` | payment, refund, support, onboarding review, audit | `Support Lead` |
| `M7` Release Hardening and Launch Readiness | `Sprint 8` | full UAT, drills, rollback, monitoring, launch checklist complete | `QA Lead` |

### 6.2 Milestone Exit Criteria

### `M0` Planning Lock

- all baseline design docs exist
- MVP scope is frozen
- workstream owners are assigned
- repo branching and delivery rules are agreed

### `M1` Platform Foundation

- Spring Boot skeleton and package boundaries exist
- Flyway baseline runs
- CI validates build and tests
- auth OTP request and verify skeleton works in dev

### `M2` Identity and Core Profiles

- customer can sign in and complete profile
- driver can sign in and submit onboarding data
- admin can sign in

### `M3` Commercial and Booking Happy Path

- serviceability check works
- quote generation works
- booking can be created from valid quote
- customer can see booking detail and history

### `M4` Dispatch and Driver Execution

- driver availability works
- assignment offer flow works
- booking can move from pending assignment to assigned
- rescue queue surfaces at-risk items

### `M5` Trip, Tracking, and Notifications

- driver arrival works
- customer start confirmation prevents early billing
- live tracking works
- core push and SMS notifications work

### `M6` Payments, Support, and Admin Controls

- completed trip payment works
- invoice works
- refund request and approval workflow works
- support and incident flows work
- driver onboarding review queue works

### `M7` Release Hardening and Launch Readiness

- MVP global acceptance criteria pass
- infra launch checklist passes
- ops launch readiness passes
- UAT sign-off complete

## 7. Epic Catalog

### 7.1 Epic Summary

| Epic ID | Epic | Owner | Depends On |
|---|---|---|---|
| `EP-00` | Program Governance and Delivery Control | `Founder / Product Owner` | none |
| `EP-01` | Platform Foundation and Module Skeleton | `Engineering Lead` | `EP-00` |
| `EP-02` | Infrastructure, CI/CD, and Observability | `Platform / DevOps Lead` | `EP-00` |
| `EP-03` | Identity and Access | `Backend Lead` | `EP-01`, `EP-02` |
| `EP-04` | Customer Core Experience | `Mobile Lead` | `EP-03`, `EP-05`, `EP-06` |
| `EP-05` | Driver Onboarding and Compliance | `Backend Lead` | `EP-03` |
| `EP-06` | Pricing, Serviceability, and Quote Engine | `Backend Lead` | `EP-01`, `EP-02` |
| `EP-07` | Booking Lifecycle | `Backend Lead` | `EP-06`, `EP-03` |
| `EP-08` | Dispatch, Assignment, and Rescue | `Ops Lead` | `EP-05`, `EP-07` |
| `EP-09` | Trip Execution and Tracking | `Mobile Lead` | `EP-08` |
| `EP-10` | Payments, Invoice, and Refunds | `Finance / Compliance Owner` | `EP-09`, `EP-07` |
| `EP-11` | Notifications and Communication | `Platform / DevOps Lead` | `EP-08`, `EP-09`, `EP-10` |
| `EP-12` | Support, Incidents, and Refund Desk | `Support Lead` | `EP-10`, `EP-11` |
| `EP-13` | Admin and Ops Console | `Admin Web Lead` | `EP-05`, `EP-07`, `EP-08`, `EP-12` |
| `EP-14` | QA, UAT, and Launch Hardening | `QA Lead` | all major delivery epics |
| `EP-15` | Ops Readiness and Launch Operations | `Ops Lead` | `EP-12`, `EP-13`, `EP-14` |

### 7.2 Epic Details

### `EP-00` Program Governance and Delivery Control

Objective:

- keep scope stable and decisions synchronized

Outputs:

- owner assignment
- sprint calendar
- release decision log
- change-control process

Done when:

- weekly review cadence exists
- scope-change requests have explicit approval path

### `EP-01` Platform Foundation and Module Skeleton

Objective:

- create the backend structure defined in the LLD

Outputs:

- Spring Boot modular monolith skeleton
- common platform utilities
- module boundaries and ArchUnit tests

Done when:

- package structure exists
- app boots in dev
- boundary tests run in CI

### `EP-02` Infrastructure, CI/CD, and Observability

Objective:

- create reliable delivery and runtime foundations

Outputs:

- environments
- CI/CD workflows
- secrets pattern
- log and metrics baseline

Done when:

- dev deploy works from CI
- staging deploy path exists
- alerts and dashboards exist for critical workflows

### `EP-03` Identity and Access

Objective:

- support secure access for customer, driver, and admin

Outputs:

- OTP auth
- admin login
- JWT and session handling
- RBAC baseline

Done when:

- customer, driver, and admin auth happy paths work

### `EP-04` Customer Core Experience

Objective:

- deliver customer MVP booking and trip user experience

Outputs:

- onboarding
- home
- quote flow
- booking flow
- trip flow
- payment and rating screens

Done when:

- customer happy path meets PRD acceptance criteria

### `EP-05` Driver Onboarding and Compliance

Objective:

- ensure only approved drivers can become eligible

Outputs:

- driver onboarding submission
- document uploads
- status tracking
- approval and correction flow

Done when:

- approved driver can go online
- unapproved driver cannot receive work

### `EP-06` Pricing, Serviceability, and Quote Engine

Objective:

- implement transparent commercial rules

Outputs:

- quote engine
- serviceability checks
- quote expiry
- cancellation preview

Done when:

- quote responses match commercial spec and remain auditable

### `EP-07` Booking Lifecycle

Objective:

- create and manage bookings cleanly

Outputs:

- booking creation
- booking detail
- modification preview and apply
- cancellation preview and apply

Done when:

- customer booking create and manage flow works end to end before assignment

### `EP-08` Dispatch, Assignment, and Rescue

Objective:

- move bookings into reliable driver assignment with ops fallback

Outputs:

- driver availability
- assignment offers
- assignment lock
- at-risk detection
- rescue queue
- manual reassignment

Done when:

- ops can detect and rescue at-risk assignments

### `EP-09` Trip Execution and Tracking

Objective:

- deliver trusted trip start, active trip, and completion behavior

Outputs:

- driver arrival
- customer start confirmation
- live tracking
- trip completion

Done when:

- trip-start confirmation prevents early billing

### `EP-10` Payments, Invoice, and Refunds

Objective:

- support clean payment capture and auditable refund handling

Outputs:

- payment order creation
- callback handling
- invoice generation
- refund initiation and approval

Done when:

- completed trip can be paid and refunded per policy

### `EP-11` Notifications and Communication

Objective:

- ensure reliable lifecycle communication

Outputs:

- push and SMS
- notification templates
- idempotent event-driven delivery
- fallback handling

Done when:

- critical lifecycle updates are sent and not contradictory

### `EP-12` Support, Incidents, and Refund Desk

Objective:

- give support and ops structured case handling tools

Outputs:

- ticket intake
- ticket actions
- incident severity handling
- refund desk workflow

Done when:

- support can triage and resolve primary categories

### `EP-13` Admin and Ops Console

Objective:

- enable day-to-day business operations

Outputs:

- dashboard
- booking queue
- rescue queue
- onboarding review
- support queue
- refund desk
- audit log
- reporting

Done when:

- ops can run primary workflows without engineering intervention

### `EP-14` QA, UAT, and Launch Hardening

Objective:

- validate behavior, stability, and readiness

Outputs:

- regression pack
- end-to-end tests
- UAT scripts
- load and failure drills

Done when:

- all acceptance gates pass and known critical defects are closed

### `EP-15` Ops Readiness and Launch Operations

Objective:

- make sure the business can operate launch-day and post-launch

Outputs:

- staffing and queue ownership
- SOP training
- shift handover template
- launch war-room process

Done when:

- ops lead signs off launch readiness checklist

## 8. Dependency Map

### 8.1 High-Level Dependencies

| From | To | Why |
|---|---|---|
| `EP-01` | `EP-03`, `EP-06`, `EP-07` | backend base and module skeleton required |
| `EP-02` | all runtime epics | environments, secrets, CI, observability required |
| `EP-03` | `EP-04`, `EP-05`, `EP-07`, `EP-13` | identities and auth gate all user journeys |
| `EP-05` | `EP-08` | only approved drivers can be assigned |
| `EP-06` | `EP-07` | booking must be created from valid quote |
| `EP-07` | `EP-08`, `EP-10`, `EP-12`, `EP-13` | booking is the base operational entity |
| `EP-08` | `EP-09`, `EP-11`, `EP-13` | trip and notifications depend on assignment |
| `EP-09` | `EP-10` | payment occurs after trip completion |
| `EP-10` | `EP-12` | refund desk depends on payment state and invoice |
| `EP-11` | `EP-14`, `EP-15` | communication behavior must be validated before launch |
| `EP-12`, `EP-13` | `EP-15` | ops launch depends on support and console readiness |

### 8.2 Critical Path Dependencies

Critical path backlog:

- `EP-01 -> EP-03 -> EP-06 -> EP-07 -> EP-08 -> EP-09 -> EP-10 -> EP-12 -> EP-14 -> EP-15`

## 9. Sprint Plan

### 9.1 Sprint 0: Planning Lock and Setup

Objective:

- prepare the team and repo for execution

| Task ID | Task | Owner | Depends On | Deliverable |
|---|---|---|---|---|
| `S0-T1` | map role-based owners to actual people | `Founder / Product Owner` | none | owner matrix |
| `S0-T2` | create sprint calendar and review rituals | `Founder / Product Owner` | none | delivery cadence |
| `S0-T3` | confirm branch and release strategy | `Engineering Lead` | none | Git workflow |
| `S0-T4` | confirm environment naming and access model | `Platform / DevOps Lead` | none | env matrix |
| `S0-T5` | convert docs into initial ticket system structure | `Engineering Lead` | none | issue tracker skeleton |
| `S0-T6` | review and sign off frozen MVP scope | `Founder / Product Owner` | none | scope lock |

Sprint Exit:

- team can begin implementation with assigned ownership

### 9.2 Sprint 1: Platform and Infra Foundation

Objective:

- establish buildable backend and deployable dev environment

| Task ID | Task | Owner | Depends On | Deliverable |
|---|---|---|---|---|
| `S1-T1` | scaffold Spring Boot modular monolith package structure | `Backend Lead` | `S0-T5` | backend skeleton |
| `S1-T2` | add common error envelope, request context, security base, and audit infrastructure | `Backend Lead` | `S1-T1` | common module |
| `S1-T3` | add Flyway baseline and PostgreSQL dev setup | `Backend Lead` | `S1-T1` | migration baseline |
| `S1-T4` | provision `dev` environment and container registry | `Platform / DevOps Lead` | `S0-T4` | dev runtime |
| `S1-T5` | set up CI workflows for backend, admin web, and mobile checks | `Platform / DevOps Lead` | `S0-T3` | CI pipeline |
| `S1-T6` | add structured logging, metrics baseline, and health endpoints | `Platform / DevOps Lead` | `S1-T4` | observability baseline |
| `S1-T7` | scaffold customer and driver mobile apps with shared navigation shells | `Mobile Lead` | `S0-T5` | app shells |
| `S1-T8` | scaffold admin web app and auth shell | `Admin Web Lead` | `S0-T5` | web shell |
| `S1-T9` | define QA smoke checklist for dev deployments | `QA Lead` | `S1-T4` | smoke checklist |

Sprint Exit:

- app skeletons exist
- dev environment deploys
- backend builds and migrations run

### 9.3 Sprint 2: Identity, Profiles, and Driver Onboarding Base

Objective:

- enable secure sign-in and basic actor profiles

| Task ID | Task | Owner | Depends On | Deliverable |
|---|---|---|---|---|
| `S2-T1` | implement customer and driver OTP request and verify APIs | `Backend Lead` | `S1-T2`, `S1-T3` | auth APIs |
| `S2-T2` | implement admin login, session, and RBAC baseline | `Backend Lead` | `S1-T2` | admin auth |
| `S2-T3` | implement customer profile and saved locations APIs | `Backend Lead` | `S2-T1` | customer profile |
| `S2-T4` | implement driver onboarding, document, and status APIs | `Backend Lead` | `S2-T1` | driver onboarding APIs |
| `S2-T5` | build customer login, OTP, profile setup, and home shell screens | `Mobile Lead` | `S2-T1`, `S2-T3` | customer onboarding flow |
| `S2-T6` | build driver login, onboarding submission, and status screens | `Mobile Lead` | `S2-T1`, `S2-T4` | driver onboarding flow |
| `S2-T7` | build admin login and onboarding review queue shell | `Admin Web Lead` | `S2-T2`, `S2-T4` | admin auth + queue shell |
| `S2-T8` | define document-review templates and correction codes | `Ops Lead` | `S2-T4` | onboarding operational pack |
| `S2-T9` | write integration tests for auth and onboarding happy paths | `QA Lead` | `S2-T1` to `S2-T7` | auth/onboarding regression pack |

Sprint Exit:

- customer, driver, and admin authentication work in dev
- driver onboarding submission path exists

### 9.4 Sprint 3: Commercial Engine and Quote Flow

Objective:

- implement serviceability and transparent quoting

| Task ID | Task | Owner | Depends On | Deliverable |
|---|---|---|---|---|
| `S3-T1` | implement serviceability config tables and APIs | `Backend Lead` | `S1-T3` | serviceability layer |
| `S3-T2` | implement pricing plans, quote engine, and quote expiry | `Backend Lead` | `S1-T3`, `S2-T3` | quote engine |
| `S3-T3` | implement quote response mapping exactly as API spec | `Backend Lead` | `S3-T2` | contract-compliant quote API |
| `S3-T4` | seed launch-city serviceability and pricing config in dev | `Ops Lead` | `S3-T1`, `S3-T2` | seed config |
| `S3-T5` | build customer service setup and quote result screens | `Mobile Lead` | `S3-T2`, `S3-T3` | quote UX |
| `S3-T6` | build admin pricing and serviceability config screens | `Admin Web Lead` | `S3-T1`, `S3-T2` | config console |
| `S3-T7` | define pricing validation cases and golden quote scenarios | `QA Lead` | `S3-T2` | commercial test pack |
| `S3-T8` | validate quote math against pricing doc and sample scenarios | `Finance / Compliance Owner` | `S3-T2` | commercial sign-off |

Sprint Exit:

- customer can get a transparent quote
- commercial math is reviewed and accepted

### 9.5 Sprint 4: Booking Lifecycle and Customer Happy Path

Objective:

- create bookings from valid quotes and show lifecycle clearly

| Task ID | Task | Owner | Depends On | Deliverable |
|---|---|---|---|---|
| `S4-T1` | implement booking creation, detail, list, and timeline APIs | `Backend Lead` | `S3-T2`, `S2-T3` | booking APIs |
| `S4-T2` | implement cancellation preview and cancellation apply | `Backend Lead` | `S4-T1`, `S3-T2` | cancellation flow |
| `S4-T3` | implement booking modification preview and apply | `Backend Lead` | `S4-T1`, `S3-T2` | modification flow |
| `S4-T4` | implement idempotency for booking create and cancellation | `Backend Lead` | `S4-T1`, `S1-T2` | idempotent booking writes |
| `S4-T5` | build booking review, pending assignment, bookings list, and booking detail screens | `Mobile Lead` | `S4-T1`, `S4-T2`, `S4-T3` | booking lifecycle UX |
| `S4-T6` | build admin booking queue and booking detail screens | `Admin Web Lead` | `S4-T1` | ops booking console |
| `S4-T7` | define customer support copy and cancellation explanation templates | `Support Lead` | `S4-T2` | customer communication pack |
| `S4-T8` | execute customer happy-path QA through booking confirmation | `QA Lead` | `S4-T1` to `S4-T6` | booking QA report |

Sprint Exit:

- customer can sign in, quote, confirm booking, and view booking state

### 9.6 Sprint 5: Dispatch, Driver Availability, and Rescue

Objective:

- move from booking to assignment with reliable operational fallback

| Task ID | Task | Owner | Depends On | Deliverable |
|---|---|---|---|---|
| `S5-T1` | implement driver availability APIs and eligibility gating | `Backend Lead` | `S2-T4` | availability service |
| `S5-T2` | implement candidate discovery, assignment offers, accept and decline | `Backend Lead` | `S4-T1`, `S5-T1` | assignment engine |
| `S5-T3` | implement rescue scanner, at-risk detection, and manual reassignment | `Backend Lead` | `S5-T2`, `S1-T2` | rescue engine |
| `S5-T4` | build driver home, availability toggle, offer, and assignment detail screens | `Mobile Lead` | `S5-T1`, `S5-T2` | driver assignment UX |
| `S5-T5` | build rescue queue and manual reassignment screens | `Admin Web Lead` | `S5-T3` | rescue console |
| `S5-T6` | define rescue queue operating rules and handoff notes | `Ops Lead` | `S5-T3` | rescue operations pack |
| `S5-T7` | add notifications for booking confirmed, assignment locked, and reassigned | `Backend Lead` | `S5-T2`, `S1-T2` | lifecycle notifications v1 |
| `S5-T8` | run race-condition and reassignment QA scenarios | `QA Lead` | `S5-T2`, `S5-T3`, `S5-T5` | dispatch QA report |

Sprint Exit:

- approved driver can go online, accept a job, and ops can rescue at-risk bookings

### 9.7 Sprint 6: Trip Start, Tracking, and Core Communication

Objective:

- execute the trip with trust-first controls

| Task ID | Task | Owner | Depends On | Deliverable |
|---|---|---|---|---|
| `S6-T1` | implement driver arrival, customer start confirmation, and trip state transitions | `Backend Lead` | `S5-T2` | trip lifecycle service |
| `S6-T2` | implement handover checklist storage and media attachment | `Backend Lead` | `S6-T1` | handover support |
| `S6-T3` | implement tracking session, location pings, SSE, and share link APIs | `Backend Lead` | `S6-T1`, `S1-T4` | tracking service |
| `S6-T4` | build customer assigned driver, start trip, active trip, SOS, and share screens | `Mobile Lead` | `S6-T1`, `S6-T3` | active trip UX |
| `S6-T5` | build driver arrived, waiting, active trip, and trip completion shell screens | `Mobile Lead` | `S6-T1`, `S6-T3` | driver trip UX |
| `S6-T6` | finalize notification templates and push plus SMS fallback flows | `Platform / DevOps Lead` | `S5-T7`, `S6-T1` | notification reliability v2 |
| `S6-T7` | define late-driver and wrong-driver operational drills | `Ops Lead` | `S6-T1`, `S6-T3` | trip ops drill pack |
| `S6-T8` | execute trip-start guard, tracking, and stale-data QA | `QA Lead` | `S6-T1` to `S6-T6` | trip QA report |

Sprint Exit:

- driver can arrive, customer confirms trip start, tracking works, and trip can proceed safely

### 9.8 Sprint 7: Payment, Refund, Support, and Admin Completion

Objective:

- complete commercial closure and support operations

| Task ID | Task | Owner | Depends On | Deliverable |
|---|---|---|---|---|
| `S7-T1` | implement payment order, callback handling, and invoice generation | `Backend Lead` | `S6-T1`, `S1-T4` | payment flow |
| `S7-T2` | implement refund APIs, approval checks, and refund audit trail | `Backend Lead` | `S7-T1`, `S1-T2` | refund workflow |
| `S7-T3` | implement support ticket, ticket actions, incident case, and refund desk APIs | `Backend Lead` | `S7-T2`, `S4-T1` | support domain |
| `S7-T4` | build customer payment, invoice, rating, and issue-reporting screens | `Mobile Lead` | `S7-T1`, `S7-T3` | post-trip UX |
| `S7-T5` | build support inbox, ticket detail, refund desk, audit log, and reporting screens | `Admin Web Lead` | `S7-T2`, `S7-T3` | support/admin completion |
| `S7-T6` | finalize refund categories, role caps, and incident escalation owners in config | `Support Lead` | `S7-T2`, `S7-T3` | support policy pack |
| `S7-T7` | validate invoice, refund, and payout records against finance expectations | `Finance / Compliance Owner` | `S7-T1`, `S7-T2` | finance sign-off |
| `S7-T8` | execute payment failure, duplicate callback, refund, and incident QA | `QA Lead` | `S7-T1` to `S7-T5` | finance/support QA report |

Sprint Exit:

- completed trip can be paid, invoiced, supported, and refunded within policy

### 9.9 Sprint 8: Hardening, UAT, and Launch Readiness

Objective:

- prove readiness across engineering, ops, support, infra, and release

| Task ID | Task | Owner | Depends On | Deliverable |
|---|---|---|---|---|
| `S8-T1` | stand up staging as full release rehearsal environment | `Platform / DevOps Lead` | `S1-T4`, `S7-T5` | staging release path |
| `S8-T2` | finalize dashboards, alerts, backup verification, and restore drill | `Platform / DevOps Lead` | `S1-T6`, `S7-T1` | launch observability pack |
| `S8-T3` | run full end-to-end regression on customer, driver, and admin flows | `QA Lead` | all feature sprints | regression report |
| `S8-T4` | run UAT with founder, ops, support, and finance sign-off | `Founder / Product Owner` | `S8-T3` | UAT sign-off |
| `S8-T5` | run ops drills: onboarding review, rescue, late driver, refund, incident, shift handover | `Ops Lead` | `S7-T5`, `S7-T6` | ops drill sign-off |
| `S8-T6` | run security and secret-management review for production readiness | `Platform / DevOps Lead` | `S8-T2` | security readiness sign-off |
| `S8-T7` | create launch-day war room plan and rollback decision tree | `Engineering Lead` | `S8-T2`, `S8-T5` | go-live runbook |
| `S8-T8` | fix critical defects and approve release candidate | `Engineering Lead` | `S8-T3` to `S8-T7` | release candidate |

Sprint Exit:

- MVP global acceptance criteria pass
- launch readiness checklists pass

## 10. Backlog by Workstream

### 10.1 Backend Workstream

Primary Owner:

- `Backend Lead`

Backlog priorities:

1. common platform and module skeleton
2. auth and sessions
3. pricing and quote
4. booking
5. dispatch
6. trip and tracking
7. finance
8. support and admin read models

### 10.2 Mobile Workstream

Primary Owner:

- `Mobile Lead`

Backlog priorities:

1. app shells and auth
2. customer quote and booking flow
3. driver onboarding and availability
4. assignment and trip flows
5. payment, rating, support

### 10.3 Admin Web Workstream

Primary Owner:

- `Admin Web Lead`

Backlog priorities:

1. admin auth shell
2. booking queue and detail
3. rescue queue and reassignment
4. onboarding review
5. support and refund desk
6. audit and reporting

### 10.4 Platform Workstream

Primary Owner:

- `Platform / DevOps Lead`

Backlog priorities:

1. dev environment
2. CI/CD
3. secrets and config
4. logging and monitoring
5. staging and production pipelines
6. backups and restore drills

### 10.5 Ops and Support Workstream

Primary Owner:

- `Ops Lead`

Backlog priorities:

1. onboarding review process
2. rescue process
3. delay handling
4. refund handling and caps
5. incident severity and escalation ownership
6. shift handover and launch operations

## 11. Release Gates and Checkpoints

### 11.1 End of Sprint Reviews

Every sprint review must answer:

- what is demonstrably working
- what remains blocked
- what dependencies slipped
- whether MVP scope is still protected

### 11.2 Milestone Review Gates

Milestone approval should require:

- demo of target flows
- unresolved blocker list
- acceptance sign-off from relevant owner

### 11.3 Pre-Launch Gates

Before launch:

- PRD global acceptance criteria pass
- infra checklist passes
- ops checklist passes
- no unresolved critical severity defect remains

## 12. Risk Management Backlog

### 12.1 Delivery Risks to Track Weekly

- backend dependency chain slips and blocks mobile
- pricing or refund ambiguity creates rework
- dispatch and rescue complexity grows faster than planned
- notification reliability is unstable in staging
- support tooling lands too late for ops rehearsal
- app-store release timing creates backend compatibility pressure

### 12.2 Mandatory Risk Mitigations

| Risk | Mitigation Owner | Mitigation |
|---|---|---|
| delayed backend modules block apps | `Engineering Lead` | hold API-first demos every sprint |
| commercial ambiguity | `Founder / Product Owner` | freeze unresolved pricing values before Sprint 3 exit |
| dispatch instability | `Ops Lead` | drill rescue flows in Sprint 5 and Sprint 8 |
| payment uncertainty | `Finance / Compliance Owner` | sandbox validation in Sprint 7 |
| launch-day operational confusion | `Ops Lead` | create war room and handover templates before release |

## 13. Recommended Tracker Structure

In Jira, Linear, GitHub Projects, or equivalent, create:

- `Epic`
- `Milestone`
- `Sprint`
- `Owner`
- `Dependency`
- `Status`
- `Risk`
- `Acceptance Evidence`

Recommended ticket labels:

- `backend`
- `mobile-customer`
- `mobile-driver`
- `admin-web`
- `platform`
- `ops`
- `support`
- `finance`
- `qa`
- `release-blocker`

## 14. Recommended Next Step

The next practical move is to turn this document into actual tickets in your tracker and assign real people against the role-based owners listed here. If you want, I can do the next layer too and create a `Ticket_Breakdown.md` with individual implementation tickets under each sprint task.

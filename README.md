# Rydvrse

Rydvrse is a `scheduled-first private driver platform` for people who already own cars and want a verified driver to operate their vehicle with clear pricing, stronger trip control, and reliable operational support.

This repository currently serves as the `product, architecture, operations, and execution design workspace` for the Rydvrse MVP.

## Table of Contents

- [What Rydvrse Is](#what-rydvrse-is)
- [Problem We Are Solving](#problem-we-are-solving)
- [MVP Positioning](#mvp-positioning)
- [Core Product Principles](#core-product-principles)
- [Who the MVP Is For](#who-the-mvp-is-for)
- [In-Scope Services](#in-scope-services)
- [What Makes Rydvrse Different](#what-makes-rydvrse-different)
- [Product Surfaces](#product-surfaces)
- [Operating Model](#operating-model)
- [Technology Direction](#technology-direction)
- [Repository Status](#repository-status)
- [Documentation Index](#documentation-index)
- [Recommended Reading Order](#recommended-reading-order)
- [MVP Acceptance Standard](#mvp-acceptance-standard)
- [Out of Scope for MVP](#out-of-scope-for-mvp)
- [How to Use This Repository](#how-to-use-this-repository)

## What Rydvrse Is

Rydvrse is being designed as a `trust-first alternative` in the driver-on-demand and chauffeur service category.

The product is intentionally not a broad transportation super app. It is focused on one high-value use case:

`book a verified driver for your own car, at a known time, at a known price, with clear support if something goes wrong`

The MVP is designed for low-burn execution and operational control. That means:

- one-city launch
- dense micro-zone rollout
- scheduled-first demand shaping
- transparent pricing instead of discount-heavy growth
- ops-assisted service quality instead of premature automation

## Problem We Are Solving

Private car owners who need a driver often face the same trust and reliability problems:

- uncertainty about whether a driver will actually be assigned on time
- hidden or confusing final billing
- weak visibility into who the driver is
- anxiety when the driver starts the trip before the customer is ready
- poor support when delays, no-shows, or disputes happen

Drivers face a different but equally important set of problems:

- unclear earnings
- weak visibility into incentives and payout logic
- unreliable or low-quality assignments
- poor support when disputes occur

Operations teams in this category usually struggle with:

- late or failed assignments
- manual firefighting
- poor evidence during disputes
- inconsistent refund decisions
- weak live visibility into booking health

Rydvrse is being designed to solve all three layers at once:

- `customer trust`
- `driver fairness`
- `operational control`

## MVP Positioning

The MVP positioning is:

`A scheduled-first private driver platform for your own car, focused on clear pricing, trusted drivers, and reliable fulfillment.`

This positioning matters because the MVP is not trying to win by doing everything. It is trying to win by doing a narrower problem much better than noisy, ambiguous, or operationally inconsistent alternatives.

## Core Product Principles

These principles guide every product, design, engineering, and ops decision in this repo:

- `Trust before scale`
- `Clarity before cleverness`
- `Scheduled-first efficiency`
- `Driver fairness is a product feature`
- `Ops is part of the product`
- `Positive unit economics over growth-by-burn`

In practical terms, every key flow should answer:

- who is coming
- when they will arrive
- how much the booking will cost
- when the trip actually starts
- what the customer can do if something goes wrong

## Who the MVP Is For

Primary customer segments:

- urban professionals with private cars
- family coordinators booking for spouse, parents, or guests
- airport travelers using their own car
- late-night return users who prioritize safety and trust
- repeat users who value reliability over promo-heavy pricing

Primary driver segment:

- independent licensed drivers
- drivers who prefer scheduled work over chaotic instant demand
- drivers who value payout transparency and predictable assignments

Internal business users:

- operations executives
- support team
- driver onboarding and compliance reviewers
- finance and refund reviewers
- city manager or head ops

## In-Scope Services

The MVP supports these service types:

| Service Type | Purpose | Commercial Model |
|---|---|---|
| `Scheduled Local` | errands, meetings, shopping, local commute | time-based with minimum duration |
| `Scheduled One-Way Drop` | point A to point B in the customer's own car | base fare plus return allowance |
| `Scheduled Round Trip` | waiting plus return or multi-stop service | time-based |
| `Airport Pickup/Drop` | airport movement in customer's own car | simplified fixed or zone-based fare |
| `Late-Night Safe Return` | trust-first night use case | supported base service plus night surcharge |

## What Makes Rydvrse Different

Rydvrse is intended to feel better than legacy or inconsistent driver-on-demand products because of a few deliberate choices:

- `Customer-confirmed trip start`
  Billing should not begin before the customer confirms the driver has arrived and the trip should start.

- `Transparent pricing before confirmation`
  The customer sees fare components before booking, including night surcharge, one-way logic, and cancellation outcomes where applicable.

- `Backup and rescue as a first-class flow`
  Reassignment is not an afterthought. The system and ops both support rescue before failure becomes a bad customer experience.

- `Trust profile for the assigned driver`
  The customer should see who is coming, not just a vague assignment state.

- `Driver-friendly economics`
  Drivers should see earning preview before accepting work, and earnings should remain explainable after completion.

- `Ops-assisted reliability`
  In the early phase, human-led rescue and support are treated as strengths, not as signs the product is unfinished.

## Product Surfaces

The MVP includes three core product surfaces:

### Customer App

The customer app handles:

- OTP login
- booking input
- quote generation
- booking confirmation
- assigned driver visibility
- trip-start confirmation
- live trip tracking
- SOS and support
- payment, invoice, rating, and issue reporting

### Driver App

The driver app handles:

- OTP login
- onboarding and document submission
- approval status
- online and offline availability
- job offers and accept or decline
- arrival updates
- trip execution
- earnings visibility
- support access

### Admin and Ops Console

The admin and ops console handles:

- booking queue monitoring
- at-risk rescue queue
- manual assignment and reassignment
- driver onboarding review
- support ticket and incident handling
- refund desk
- pricing and serviceability configuration
- audit and operational reporting

## Operating Model

Rydvrse is being designed as a `low-capital, low-burn, operations-aware business`.

That means:

- one-city launch
- 2-3 dense micro-zones first
- no owned vehicle fleet
- no multi-city rollout in MVP
- no cashback or subsidy war
- no microservices-first architecture

The operational model is just as important as the app experience. The business depends on:

- strong onboarding quality
- fast rescue handling
- consistent refund logic
- visible SLA ownership
- auditable support actions

## Technology Direction

The planned technology stack and architecture direction are:

- `Spring Boot modular monolith` for backend
- `React Native` for customer and driver mobile apps
- `React-based admin web` for ops and support console
- `PostgreSQL + PostGIS` for transactional and geo data
- `Redis` for hot state, caching, rate limiting, and tracking acceleration
- `Flyway` for schema migrations
- `event-driven outbox + worker jobs` for async workflows

Core backend domains include:

- auth
- customer
- driver
- pricing
- booking
- dispatch
- trip
- tracking
- finance
- notifications
- support
- admin and reporting

## Repository Status

This repository is currently `documentation-first`.

At this stage, it contains the baseline documents needed to move confidently into implementation:

- strategy and architecture
- product scope and requirements
- screen and UX flows
- pricing and payout rules
- database schema
- API contract
- low-level backend design
- infrastructure and release plan
- ops SOP
- implementation backlog

This means the project has crossed from vague brainstorming into `execution-ready planning`.

## Documentation Index

The following documents form the current source-of-truth set for the MVP:

| Document | Purpose | Primary Audience |
|---|---|---|
| [`High_Level_Design.md`](./High_Level_Design.md) | product, architecture, workflows, module boundaries, strategic design | founders, engineering leads, product, ops |
| [`MVP_Scope.md`](./MVP_Scope.md) | exact MVP scope freeze, constraints, and exclusions | founders, product, engineering |
| [`Product_Requirements_Document.md`](./Product_Requirements_Document.md) | detailed business requirements, user stories, edge cases, and acceptance criteria | product, engineering, QA, ops |
| [`Screen_Flow.md`](./Screen_Flow.md) | screen-by-screen flows for customer, driver, and admin | design, frontend, backend, QA |
| [`Wireframes_and_UX_States.md`](./Wireframes_and_UX_States.md) | low-fidelity wireframes and UX states including edge scenarios | design, frontend, QA |
| [`Pricing_and_Payout_Design.md`](./Pricing_and_Payout_Design.md) | fare rules, payout rules, cancellation, refunds, and overrides | product, finance, backend, ops |
| [`Database_Schema.md`](./Database_Schema.md) | detailed OLTP schema, state models, indexes, audit, and outbox tables | backend, platform, data |
| [`API_Spec.md`](./API_Spec.md) | external API contract for customer, driver, admin, auth, and webhooks | backend, frontend, QA |
| [`Low_Level_Design.md`](./Low_Level_Design.md) | Spring Boot module internals, service classes, repositories, async jobs, tracking and notifications | backend engineering |
| [`Infrastructure_and_Delivery_Plan.md`](./Infrastructure_and_Delivery_Plan.md) | environments, CI/CD, migrations, secrets, logging, monitoring, backups, release plan | platform, engineering lead |
| [`Ops_SOP.md`](./Ops_SOP.md) | driver onboarding SOP, rescue handling, refunds, incidents, support SLA, shift governance | ops, support, finance |
| [`Implementation_Backlog.md`](./Implementation_Backlog.md) | milestones, epics, sprint sequencing, dependencies, and owners | founders, engineering, ops, QA |

## Recommended Reading Order

Different readers should enter the docs from different places.

### If You Are a Founder or Product Owner

Read in this order:

1. [`High_Level_Design.md`](./High_Level_Design.md)
2. [`MVP_Scope.md`](./MVP_Scope.md)
3. [`Product_Requirements_Document.md`](./Product_Requirements_Document.md)
4. [`Pricing_and_Payout_Design.md`](./Pricing_and_Payout_Design.md)
5. [`Implementation_Backlog.md`](./Implementation_Backlog.md)

### If You Are a Backend Engineer

Read in this order:

1. [`Product_Requirements_Document.md`](./Product_Requirements_Document.md)
2. [`Database_Schema.md`](./Database_Schema.md)
3. [`API_Spec.md`](./API_Spec.md)
4. [`Low_Level_Design.md`](./Low_Level_Design.md)
5. [`Infrastructure_and_Delivery_Plan.md`](./Infrastructure_and_Delivery_Plan.md)

### If You Are a Mobile or Frontend Engineer

Read in this order:

1. [`Product_Requirements_Document.md`](./Product_Requirements_Document.md)
2. [`Screen_Flow.md`](./Screen_Flow.md)
3. [`Wireframes_and_UX_States.md`](./Wireframes_and_UX_States.md)
4. [`API_Spec.md`](./API_Spec.md)
5. [`Implementation_Backlog.md`](./Implementation_Backlog.md)

### If You Are Ops, Support, or Finance

Read in this order:

1. [`MVP_Scope.md`](./MVP_Scope.md)
2. [`Pricing_and_Payout_Design.md`](./Pricing_and_Payout_Design.md)
3. [`Ops_SOP.md`](./Ops_SOP.md)
4. [`Infrastructure_and_Delivery_Plan.md`](./Infrastructure_and_Delivery_Plan.md)
5. [`Implementation_Backlog.md`](./Implementation_Backlog.md)

## MVP Acceptance Standard

The MVP should only be considered launch-ready if all of the following are true:

- a first-time customer can sign in, request a quote, book, start, complete, pay, and rate a ride without support intervention in the happy path
- a returning customer can rebook successfully
- a driver can onboard, be approved, accept a job, mark arrived, wait for customer start confirmation, complete the trip, and see earnings
- ops can detect and rescue at-risk assignments
- support can triage and resolve the primary ticket categories
- pricing stays clear and auditable through trip completion
- no hidden charge can appear outside approved or audited flows
- trip-start confirmation prevents early billing by default
- notifications, payments, and tracking are reliable enough for live operations

## Out of Scope for MVP

The MVP does not include:

- subscriptions
- prepaid ride credits
- preferred drivers
- family account hierarchy
- recurring commute automation
- corporate dashboards and billing
- wallet or cashback platform
- loyalty points
- AI pricing or AI dispatch
- chatbot support
- outstation multi-day service
- rental-car inventory
- multi-city rollout

These are intentionally deferred so the product can reach market with reliability instead of unnecessary breadth.

## How to Use This Repository

Use this repository as a `controlled planning and execution source of truth`.

Recommended rules:

- do not change scope casually after `MVP_Scope.md` is frozen
- if business rules change, update the owning design document first
- if implementation needs to deviate from the docs, record that explicitly instead of letting drift happen silently
- treat pricing, refund, onboarding, and incident processes as controlled systems, not ad hoc team behavior
- keep documentation and implementation aligned through milestone reviews

If implementation starts next, the most practical artifact to derive from this repository is the sprint and ticket tracker based on [`Implementation_Backlog.md`](./Implementation_Backlog.md).

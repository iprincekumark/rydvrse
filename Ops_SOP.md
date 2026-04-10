# Rydvrse Operations and Support SOP

## Document Control

- Document Name: `Ops_SOP.md`
- Product: `Rydvrse`
- Version: `1.0`
- Status: `Baseline Operations and Support SOP for MVP`
- Last Updated: `2026-04-10`
- Source Documents:
  - `High_Level_Design.md`
  - `MVP_Scope.md`
  - `Product_Requirements_Document.md`
  - `Screen_Flow.md`
  - `Pricing_and_Payout_Design.md`
  - `API_Spec.md`
  - `Low_Level_Design.md`
  - `Infrastructure_and_Delivery_Plan.md`
- Intended Audience:
  - founders
  - ops team
  - support team
  - finance and compliance
  - engineering and QA

## 1. Purpose

This document defines how Rydvrse should actually run day to day in MVP.

It covers:

- driver onboarding SOP
- booking rescue SOP
- late driver SOP
- refund and commercial exception SOP
- incident escalation SOP
- support SLA
- ticket triage and handoff
- audit and shift governance

This document exists because for Rydvrse, `ops quality is part of the product`.

## 2. Scope

This SOP applies to the MVP only and assumes:

- one launch city
- 2-3 dense operating zones
- scheduled-first booking model
- ops-in-the-loop rescue
- support and incidents handled through internal team, not chatbot automation

This document does not define:

- HR staffing policies
- legal language for terms and privacy
- payroll processing details
- enterprise BPO outsourcing model

## 3. Operating Principles

The ops team must follow these non-negotiable principles:

- prioritize customer trust over short-term convenience
- protect driver fairness, not just customer appeasement
- never improvise pricing or refund logic outside defined policy
- record all impactful actions with reason and timestamp
- act early on risk; do not wait for failure when signals are visible
- keep communication calm, specific, and non-contradictory
- when in doubt on safety, escalate upward

## 4. Operating Team Structure

### 4.1 Core Roles

| Role | Primary Responsibility |
|---|---|
| `Ops Executive` | booking monitoring, rescue, reassignment, customer coordination |
| `Support L1` | first-response support, ticket triage, note capture, escalation routing |
| `Support Lead` | complex complaint handling, small refund approvals, SLA supervision |
| `Driver Reviewer` | onboarding review, document quality check, approval or correction |
| `Ops Lead` | high-risk rescue decisions, severe delay handling, driver misconduct review |
| `Finance Admin` | refund processing, payment issue resolution, refund reconciliation |
| `City Manager / Head Ops` | escalation approval, policy exceptions, daily KPI ownership |

### 4.2 Shift Assumptions for MVP

Exact launch support hours are still a business decision, so this SOP uses a recommended baseline:

- core customer support: `07:00 to 23:00`
- trip monitoring and rescue: service operating hours plus `30 minutes` pre-open and post-close buffer
- severe safety escalation: always-on emergency escalation contact

If launch hours differ, update this document before production launch.

### 4.3 Queue Ownership

| Queue | Primary Owner | Secondary Owner |
|---|---|---|
| Driver onboarding review | `Driver Reviewer` | `Ops Lead` |
| Booking queue | `Ops Executive` | `Ops Lead` |
| At-risk rescue queue | `Ops Executive` | `Ops Lead` |
| Support inbox | `Support L1` | `Support Lead` |
| Refund desk | `Support Lead` or `Finance Admin` depending on category | `Ops Lead` |
| Incident queue | `Support Lead` | `Ops Lead` or `Head Ops` |

## 5. Operational State Model

Ops should think in clear queue states, not vague status notes.

### 5.1 Onboarding Review States

- `NEW`
- `INCOMPLETE`
- `UNDER_REVIEW`
- `CORRECTION_REQUIRED`
- `APPROVED`
- `REJECTED`
- `SUSPENDED`

### 5.2 Rescue States

- `MONITORING`
- `AT_RISK`
- `RESCUE_IN_PROGRESS`
- `REASSIGNED`
- `CUSTOMER_CONTACTED`
- `FAILED_FULFILLMENT`
- `RESOLVED`

### 5.3 Support Ticket States

- `OPEN`
- `TRIAGED`
- `WAITING_INTERNAL`
- `WAITING_CUSTOMER`
- `WAITING_DRIVER`
- `ESCALATED`
- `RESOLVED`
- `CLOSED`
- `REOPENED`

### 5.4 Incident States

- `OPEN`
- `TRIAGED`
- `INVESTIGATING`
- `ACTION_TAKEN`
- `RESOLVED`
- `CLOSED`

## 6. Driver Onboarding SOP

### 6.1 Objective

Allow only verified, compliant, and operationally suitable drivers to become assignable.

### 6.2 Intake Checklist

Required minimum onboarding inputs:

- mobile OTP verified account
- full name
- city selection
- driving license
- Aadhaar or approved ID proof
- PAN if required for payouts
- selfie or face match step if implemented
- bank details
- emergency contact

Required document quality standards:

- readable text
- all corners visible
- not expired
- name and identity consistent with profile
- no obvious tampering

### 6.3 Onboarding Review Workflow

1. Driver submits onboarding and documents.
2. System places record into `UNDER_REVIEW`.
3. `Driver Reviewer` opens queue in oldest-first or SLA-risk order.
4. Reviewer checks document completeness.
5. Reviewer checks document quality.
6. Reviewer checks expiry dates.
7. Reviewer checks data consistency across records.
8. Reviewer takes one of three actions:
   - approve
   - request correction
   - reject
9. Action is audited with reason and reviewer notes.

### 6.4 Approval Criteria

Approve only if all are true:

- all mandatory fields present
- all mandatory documents uploaded
- documents readable and valid
- no expired compliance blockers
- bank details acceptable for payout setup
- no duplicate or suspicious identity signal found

### 6.5 Correction Request SOP

Use `CORRECTION_REQUIRED` when:

- image is blurred or cropped
- mismatch seems fixable
- one or more documents missing
- bank details invalid or incomplete

Required reviewer behavior:

- specify exact item needing correction
- avoid vague notes like "invalid document"
- list one or more concrete next steps
- never approve partially complete profile with a promise to fix later

Example correction note style:

- "Driving license image is blurred; please re-upload front side with all four corners visible."
- "PAN number in profile does not match uploaded PAN card; update profile or re-upload correct document."

### 6.6 Rejection SOP

Reject when:

- identity appears fraudulent
- prohibited or unsupported document type provided
- repeated correction attempts fail on critical compliance items
- risk signal is serious enough that correction flow is not appropriate

Rejection must include:

- reason code
- human-readable explanation
- whether future reapplication is allowed

### 6.7 Approval SLA

Recommended target:

- first review start within `4 business hours`
- final outcome within `24 business hours`

### 6.8 Post-Approval Controls

After approval:

- driver becomes eligible for availability and assignment
- ops should verify first live status visibility
- document expiry monitoring remains active

If a document expires after approval:

- driver must be marked ineligible automatically or via ops action
- any active future assignment eligibility should be blocked
- if already on an active trip, trip completes first, then future assignment blocked

### 6.9 Onboarding QA Audit

Driver Reviewer or Ops Lead should sample approved records daily for:

- incorrect approvals
- inconsistent notes
- repeated reviewer errors

## 7. Booking Rescue SOP

### 7.1 Objective

Prevent assignment failure from turning into customer-visible service failure whenever reasonable recovery is possible.

### 7.2 Rescue Triggers

A booking enters `AT_RISK` when one or more apply:

- no driver accepted in expected threshold
- assigned driver cancels
- assigned driver is not moving toward pickup
- ETA exceeds SLA risk threshold
- driver device goes offline after acceptance
- wrong driver or trust mismatch reported before trip start

### 7.3 Queue Priority Rules

Sort rescue queue by:

1. nearest pickup time
2. active SLA breach risk
3. customer already impacted
4. premium trust risk, such as airport or late-night

### 7.4 Rescue Workflow

1. System flags booking `AT_RISK`.
2. `Ops Executive` opens rescue item immediately.
3. Ops verifies current booking state and current assigned driver state.
4. Ops checks whether automatic reassignment is already in progress.
5. Ops chooses next action:
   - continue monitoring if the driver is still recoverable
   - manually reassign
   - contact driver
   - contact customer
   - mark failed fulfillment if recovery is not realistic
6. All rescue actions must be logged.
7. Customer-visible updates must reflect latest state only.

### 7.5 Rescue Decision Tree

### Case A: No Driver Assigned Yet

- run manual candidate search
- reassign immediately if eligible driver available
- if no candidate found in acceptable window:
  - inform customer honestly
  - offer cancellation without fee
  - if prepaid, ensure full refund initiation

### Case B: Driver Accepted but Not Moving

- contact driver once
- if unresponsive or not progressing, reassign
- do not let the customer wait on vague promises

### Case C: Driver Cancelled

- reassign immediately if feasible
- customer must never be charged for driver-caused cancellation
- if no recovery possible, mark `FAILED_FULFILLMENT`

### Case D: ETA Breach Predicted

- if still recoverable and close to pickup, continue monitor with note
- if likely to miss commitment materially, start reassignment before visible failure if possible

### 7.6 Customer Communication Rules During Rescue

- do not say "driver is reaching in 2 minutes" unless that is actually defensible
- communicate exact next step, not just apology
- if reassignment is in progress, say so clearly
- if failure is likely, set expectation early

### 7.7 Rescue SLA

Recommended targets:

- acknowledge rescue item within `2 minutes`
- take first meaningful action within `5 minutes`
- resolve standard rescue decision within `10 minutes`

### 7.8 Failed Fulfillment SOP

Mark `FAILED_FULFILLMENT` only when:

- no safe or reasonable replacement exists
- timing no longer supports meaningful service recovery
- customer declines reassigned option

Required actions:

- zero customer cancellation fee
- prepaid amount refunded in full
- support note logged
- root-cause tag applied

## 8. Late Driver Handling SOP

### 8.1 Objective

Handle lateness consistently before it damages trust.

### 8.2 Late Driver Classification

### Minor Delay

- projected delay within small tolerance
- booking may still be recoverable without reassignment

### Material Delay

- projected delay likely to materially affect customer experience
- likely requires reassignment or proactive customer communication

### Severe Delay / No-Show Risk

- driver is stationary, offline, or clearly not going to make pickup
- must be treated as rescue immediately

Exact thresholds should be configured city-wise, but ops must not wait for severe breach if recovery window is shrinking.

### 8.3 Late Driver Workflow

1. System or ops detects delay.
2. Verify whether ETA data is fresh or stale.
3. Contact driver if recovery is plausible.
4. Decide whether to:
   - continue monitoring
   - reassign
   - notify customer of updated ETA
   - offer no-fee cancellation
5. If ride still completes after material delay, support goodwill may apply per refund policy.

### 8.4 Customer Goodwill Guidance for Completed Late Ride

Default recommendation when completed ride had material delay and no better recovery was offered:

- `49` or `99` goodwill refund
- or up to `20% of service fee ex tax`, whichever is lower

Approval follows refund matrix in Section 10.

### 8.5 Driver Accountability for Delay

For MVP, avoid automatic monetary penalties except where policy later changes.

Use:

- reliability score impact
- dispatch deprioritization
- incentive ineligibility
- repeated severe delay review

## 9. Support Ticket SOP

### 9.1 Ticket Intake Channels

Allowed ticket sources:

- customer app
- driver app
- admin-created internal case
- payment or operational exception flow

### 9.2 Ticket Categories

Minimum categories:

- billing and pricing
- driver delay
- driver no-show
- behavior and professionalism
- trust or identity mismatch
- safety incident
- app or technical issue
- payment issue
- driver payout issue

### 9.3 Triage Workflow

1. Ticket created with booking or trip context where available.
2. `Support L1` reviews within SLA.
3. Severity assigned.
4. Owner assigned.
5. Case routed:
   - resolve at L1
   - escalate to Support Lead
   - escalate to Ops
   - escalate to Finance
   - escalate to Incident flow

### 9.4 Mandatory Ticket Notes

Every meaningful support action must record:

- actor
- timestamp
- summary of action
- next expected step
- customer callback or update status

### 9.5 Ticket Resolution Rule

Do not close a ticket if:

- refund was promised but not initiated
- incident investigation is pending
- customer requested callback is still due
- owner handoff happened without a resolution note

## 10. Refund Approval SOP

### 10.1 Objective

Keep refunds fair, fast, auditable, and consistent with unit economics.

### 10.2 Refund Principles

- every refund must have a category
- every refund must link to a booking, payment, or incident
- no free-form guesswork
- no refund should be used to hide operational root cause
- exact overcharge corrections should be preferred over arbitrary goodwill

### 10.3 Refund Categories

- failed fulfillment
- duplicate charge
- payment technical issue
- pricing mismatch
- driver delay
- driver no-show
- wrong driver or trust breach
- service quality issue
- safety incident
- goodwill exception

### 10.4 Refund Decision Matrix

| Category | Default Outcome | Default Refund Range | Primary Owner |
|---|---|---:|---|
| Failed Fulfillment Before Start | full refund of collected amount | 100% | `Ops / Support` |
| Duplicate Charge | refund duplicate amount | exact duplicate | `Finance Admin` |
| Payment Technical Error | refund incorrect capture | exact impacted amount | `Finance Admin` |
| Pricing Mismatch / Engine Error | refund overcharged amount | exact difference or more if justified | `Support Lead / Finance Admin` |
| Driver Delay, Ride Completed | goodwill partial refund | `49-99` or up to `20%` | `Support Lead` |
| Driver No-Show | full refund of any collected amount | 100% | `Ops Lead` |
| Wrong Driver / Trust Breach Before Start | full refund if prepaid | up to 100% | `Ops Lead` |
| Service Quality Issue After Completion | partial or full based on severity | `10%-100%` | `Support Lead` |
| Safety Incident / Verified Misconduct | strong corrective refund | `50%-100%` | `Ops Lead / Head Ops` |
| Goodwill Exception | bounded discretionary refund | capped by role | `Support Lead` |

### 10.5 Role Approval Matrix

| Role | Refund Approval Power | Notes |
|---|---:|---|
| `Support L1` | `0 direct approval` | may recommend only |
| `Support Lead` | up to `300` or `25% of fare`, whichever is lower | may waive cancellation fee fully |
| `Finance Admin` | approved amount only | no discretionary goodwill beyond policy |
| `Ops Lead / Head Ops` | full amount when justified | severe trust, no-show, safety, or emergency cases |

### 10.6 Refund Workflow

1. Ticket or case owner confirms refund category.
2. Owner verifies payment state and refundable amount.
3. Owner checks whether refund is exact correction, goodwill, or severe incident-linked.
4. Owner confirms approval authority.
5. Refund request is created with:
   - amount
   - reason code
   - note
   - linked ticket or incident
6. `Finance Admin` processes provider-facing refund if needed.
7. Customer is notified of refund initiation and closure.
8. Ticket is resolved only after refund state is visible or ownership of pending finance action is explicit.

### 10.7 Refund SLA

Recommended targets:

- duplicate or technical refunds: initiate within `2 hours`
- standard approved refunds: initiate within `24 hours`
- severe incident-linked refunds: initiate within `24 hours` of approval

### 10.8 Cancellation Fee Waiver SOP

Support may recommend waiver when:

- platform fault caused confusion
- driver arrival issue was platform-caused
- customer never had a fair chance to take the service

Waiver rules:

- must be logged as override
- must include reason
- must not be used to hide driver-caused operational failure tagging

### 10.9 Non-Refundable by Default

Do not auto-refund for:

- normal traffic
- customer changed mind after completed trip
- dissatisfaction without a policy basis

## 11. Incident Escalation SOP

### 11.1 Objective

Ensure safety, misconduct, and severe trust events are escalated consistently and quickly.

### 11.2 Severity Model

- `SEV1`: active safety emergency
- `SEV2`: serious trip disruption or misconduct concern
- `SEV3`: service complaint or billing issue
- `SEV4`: informational or low-impact support request

### 11.3 Incident Triggers

Open an incident case when one or more apply:

- SOS triggered
- customer reports active safety risk
- wrong driver identity or major trust breach
- serious harassment or misconduct allegation
- severe dispute with conflicting accounts and meaningful harm risk
- regulator, police, or legal contact is indicated

### 11.4 SEV1 Workflow

1. Treat as active emergency.
2. `Support L1` or `Ops Executive` escalates immediately to `Ops Lead`.
3. Attempt immediate contact with customer if safe and available.
4. Preserve all ride context and evidence.
5. Do not debate commercial outcome during emergency stage.
6. Freeze or flag involved driver account if needed.
7. `Head Ops` or designated safety owner takes command.
8. Resolution, refund, and disciplinary action happen after immediate safety handling.

### 11.5 SEV2 Workflow

1. Create incident case.
2. Assign owner within SLA.
3. Gather booking, trip, and communication logs.
4. Capture customer statement.
5. Capture driver statement if appropriate.
6. Apply temporary operational restriction on driver if warranted.
7. Decide corrective action, refund recommendation, and closure plan.

### 11.6 SEV3 Workflow

Route through structured support unless evidence indicates escalation is needed.

Examples:

- pricing dispute
- completed ride delay complaint
- professionalism complaint without safety dimension

### 11.7 Evidence Collection SOP

Evidence may include:

- trip timeline
- location history
- handover notes
- call and communication metadata where available
- uploaded photos or screenshots
- support notes

Rules:

- preserve evidence before taking punitive actions when possible
- do not delete or overwrite conflicting evidence
- note if evidence arrived after closure and reopening occurred

### 11.8 Incident SLA

Recommended targets:

- `SEV1`: immediate escalation, no queue wait
- `SEV2`: owner assigned within `15 minutes`
- `SEV3`: triage within standard support SLA
- `SEV4`: normal queue handling

### 11.9 Driver Restriction Guidance During Incident

Temporarily block future assignments when:

- active safety allegation is credible
- wrong driver identity is verified
- repeated severe misconduct pattern is present

Do not silently keep the driver live during a credible severe case just to preserve supply.

## 12. Support SLA Policy

### 12.1 SLA Model

SLA should measure:

- first response time
- ownership assignment time
- resolution time
- callback or update cadence

### 12.2 Recommended SLA Matrix

| Severity | First Response | Owner Assignment | Update Cadence | Target Resolution |
|---|---:|---:|---:|---:|
| `SEV1` | immediate | immediate | continuous until stabilized | same incident window |
| `SEV2` | `10 minutes` | `15 minutes` | every `30 minutes` until stable | within `4 hours` or handoff with owner |
| `SEV3` | `30 minutes` | `60 minutes` | every `4 hours` or meaningful milestone | within `24 hours` |
| `SEV4` | `4 business hours` | same business day | daily if still open | within `2 business days` |

### 12.3 After-Hours Rule

If case arrives outside support hours:

- `SEV1` still escalates immediately
- `SEV2` should route to on-duty ops or first available escalation owner
- `SEV3` and `SEV4` may wait until next support window, but customer-facing acknowledgement should still exist if automation supports it

## 13. Shift Handover SOP

### 13.1 Objective

Prevent unresolved work from disappearing across shifts.

### 13.2 Mandatory Handover Items

Every shift handover must include:

- open rescue items
- active severe incidents
- refunds awaiting finance action
- driver reviews pending urgent decision
- tickets approaching SLA breach
- technical issues affecting ops workflows

### 13.3 Handover Format

Use a structured handover note with:

- issue ID
- current state
- owner
- what has been done
- next required action
- deadline or SLA risk

### 13.4 Shift Change Rule

No high-risk item should be handed over with only "please check" or similar vague notes.

## 14. Audit and Quality Control SOP

### 14.1 Actions That Must Be Audited

- driver approval, rejection, correction request, suspension
- manual reassignment
- failed fulfillment marking
- cancellation fee waiver
- refund initiation and approval
- pricing or serviceability overrides
- incident resolution actions

### 14.2 Daily QA Review

Ops Lead or City Manager should review a daily sample of:

- rescued bookings
- refunded bookings
- approved and rejected drivers
- severe support tickets

QA review checks:

- was policy followed
- was communication clear
- was root cause tagged correctly
- was refund amount consistent with matrix
- was audit note sufficient

## 15. KPI Ownership for Ops

The ops team should own and review at minimum:

- fulfillment rate
- assignment time
- at-risk queue count
- rescue success rate
- driver no-show count
- on-time arrival rate
- refund rate
- average refund amount
- support ticket volume
- incident count by severity
- onboarding approval turnaround time

## 16. Escalation Matrix

| Situation | Primary Owner | Escalate To |
|---|---|---|
| Incomplete driver document | `Driver Reviewer` | `Ops Lead` |
| Fraud-suspect onboarding | `Driver Reviewer` | `Head Ops` |
| No driver assigned near pickup | `Ops Executive` | `Ops Lead` |
| Driver cancelled close to pickup | `Ops Executive` | `Ops Lead` |
| Delay complaint after completed ride | `Support L1` | `Support Lead` |
| Duplicate charge | `Support L1` | `Finance Admin` |
| Pricing mismatch claim | `Support Lead` | `Finance Admin` |
| SEV1 safety case | first receiver | `Ops Lead / Head Ops` immediately |
| Full refund request beyond role cap | current owner | `Ops Lead / Head Ops` |

## 17. Launch Readiness Checklist for Ops

Before go-live, confirm:

- driver onboarding checklist is finalized
- review reasons and templates exist
- rescue queue is tested end to end
- late-driver path is tested with real staff drills
- refund reason codes and approval caps are configured
- incident severity and escalation owners are named
- support SLA dashboard exists
- shift handover template exists
- audit logs are visible to authorized leads
- ops team knows who owns emergency escalation after hours

## 18. Recommended Next Step

The next best artifact after this SOP is an `Implementation_Backlog.md` that turns the product, backend, infra, and ops documents into concrete build and launch tasks across engineering, ops, finance, and support.

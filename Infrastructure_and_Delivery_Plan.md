# Rydvrse Infrastructure and Delivery Plan

## Document Control

- Document Name: `Infrastructure_and_Delivery_Plan.md`
- Product: `Rydvrse`
- Version: `1.0`
- Status: `Baseline Infrastructure and Delivery Plan for MVP`
- Last Updated: `2026-04-10`
- Source Documents:
  - `High_Level_Design.md`
  - `MVP_Scope.md`
  - `Product_Requirements_Document.md`
  - `Database_Schema.md`
  - `API_Spec.md`
  - `Low_Level_Design.md`
- Intended Audience:
  - founders
  - engineering
  - DevOps and platform
  - security and compliance reviewers
  - operations and support

## 1. Purpose

This document defines how Rydvrse should be deployed, operated, monitored, and released across environments.

It covers:

- environments
- deployment topology
- CI/CD
- secrets and configuration handling
- database migrations
- logging and observability
- alerting
- backups and restore strategy
- rollback and release governance

This plan is designed for a `low-burn MVP` that is still clean enough to scale without needing a full re-platform later.

## 2. Guiding Principles

The infrastructure plan must follow these rules:

- use managed services where they reduce operational risk
- keep the MVP simple enough for a small team to operate
- separate environments clearly
- never treat production like a test bed
- automate deployments, but keep manual approval for production
- keep secrets out of code and out of mobile binaries
- prefer forward-fix and rollback-by-redeploy over risky manual patching
- monitor business-critical workflows, not just CPU and memory

## 3. Reference Platform Choice

To remove ambiguity, this plan uses `AWS` as the reference cloud for MVP.

Why AWS for MVP:

- mature managed PostgreSQL, Redis, object storage, and load balancing
- clear private networking model
- strong monitoring and secrets tooling
- easy path from one service to multiple services later

Reference service mapping:

- compute: `ECS Fargate`
- database: `RDS PostgreSQL`
- cache: `ElastiCache Redis`
- object storage: `S3`
- CDN and web delivery: `CloudFront` + static hosting
- container registry: `ECR`
- secrets: `AWS Secrets Manager`
- config: `SSM Parameter Store` or Secrets Manager depending on sensitivity
- logs and metrics: `CloudWatch` + OpenTelemetry exporter

If needed later, this design can be translated to GCP or Azure without changing the architectural intent.

## 4. Environment Strategy

### 4.1 Environment List

Rydvrse should operate with four core environments:

- `local`
- `dev`
- `staging`
- `production`

Optional future environments:

- `preview` for static admin web only
- `performance` for load testing if traffic and team size justify it

### 4.2 Environment Objectives

| Environment | Purpose | Data Type | Deploy Trigger | Access |
|---|---|---|---|---|
| `local` | developer productivity | local/dev seed data | manual | developers only |
| `dev` | integration testing and shared QA | synthetic seed data | automatic on merge to main or developable branch strategy | engineering and QA |
| `staging` | pre-production validation, UAT, release rehearsal | synthetic or masked data only | automatic after passing dev or manual promote | engineering, QA, ops |
| `production` | live customer traffic | real data | manual approval only | live users and authorized internal staff |

### 4.3 Environment Rules

- production data must never be copied to dev without masking and approval
- staging should be prod-like in topology, but smaller in size
- dev can be smaller and more flexible, but must still use real integrations where needed for critical paths
- all environment-specific values must come from environment config, not code branches

### 4.4 Environment Topology by Stage

### Local

- Spring Boot app running locally
- local PostgreSQL via Docker
- local Redis via Docker
- local object storage emulator optional
- provider integrations mocked or sandboxed

### Dev

- one backend service
- one worker process or scheduler-enabled backend instance
- one shared PostgreSQL instance
- one shared Redis instance
- one shared object storage bucket
- sandbox payment and messaging providers

### Staging

- backend API service
- separate worker service preferred
- managed PostgreSQL
- managed Redis
- dedicated staging object storage bucket
- real push infrastructure, sandbox payment, staging SMS if available
- restricted admin access

### Production

- backend API service behind load balancer
- separate worker service from the same container image
- managed PostgreSQL with backups and PITR
- managed Redis
- dedicated production object storage bucket with versioning
- production payment and messaging providers
- admin access protected and audited

## 5. Runtime Topology

### 5.1 Services to Deploy

For MVP, deploy these runtime artifacts:

| Artifact | Runtime Form | Purpose |
|---|---|---|
| `backend-api` | Spring Boot container | customer, driver, admin HTTP APIs |
| `backend-worker` | Spring Boot container with worker profile | outbox dispatch, notification jobs, reconciliation, SLA jobs |
| `admin-web` | static web app or SPA | internal ops and admin console |
| `customer-mobile` | React Native app | distributed via app stores and testing channels |
| `driver-mobile` | React Native app | distributed via app stores and testing channels |

### 5.2 Why Separate API and Worker in Staging and Production

Even though the codebase is one modular monolith, separating API and worker runtime profiles gives:

- better isolation for job spikes
- cleaner scaling
- fewer surprises when long-running jobs compete with API latency

Recommended profiles:

- `SPRING_PROFILES_ACTIVE=api`
- `SPRING_PROFILES_ACTIVE=worker`

The worker uses the same codebase and same database, but only enables scheduled jobs, outbox consumers, and provider polling.

### 5.3 Networking

Production and staging should use:

- public load balancer only for API and admin web
- private subnets for PostgreSQL and Redis
- no direct public access to PostgreSQL
- no public SSH access to runtime hosts
- security groups restricted by service role

## 6. Environment Configuration Model

### 6.1 Configuration Categories

Configuration should be separated into:

- non-secret runtime config
- secret config
- per-environment feature flags
- per-city or business config in the database

### 6.2 Non-Secret Runtime Config Examples

- app environment name
- log level
- API base URLs
- feature toggles safe for env variables
- worker concurrency settings
- notification retry intervals
- quote expiry defaults

### 6.3 Secret Config Examples

- database credentials
- Redis auth token if enabled
- JWT signing keys
- payment gateway secrets
- SMS provider secrets
- push provider server keys
- admin session encryption keys
- webhook verification secrets

### 6.4 Configuration Ownership

| Type | Storage |
|---|---|
| non-sensitive app config | Parameter Store or environment config files in deployment templates |
| secrets | Secrets Manager |
| business rules like pricing and zones | PostgreSQL config tables |
| mobile public runtime values | remote config or app config without secrets |

## 7. Secrets Handling

### 7.1 Rules

- no secrets in Git
- no secrets in `.env.example`
- no secrets in React Native bundles
- no manual secret copying between environments
- secrets must be rotated with documented owners

### 7.2 Storage and Access

Use:

- `AWS Secrets Manager` for production and staging secrets
- limited IAM roles so services can read only their own secrets
- developer access to production secrets only by explicit role and approval

### 7.3 Secret Injection

Secrets should be injected at runtime through:

- ECS task definition secret references
- environment variable injection from Secrets Manager

Never bake secrets into:

- container images
- frontend build artifacts
- mobile apps

### 7.4 Secret Rotation Policy

| Secret Type | Rotation Target |
|---|---|
| database credentials | every `90 days` or on incident |
| JWT signing keys | planned rotation at least every `180 days` with overlap window |
| payment and provider secrets | on provider rotation policy or incident |
| admin bootstrap credentials | immediately after environment provisioning |

### 7.5 Mobile Security Rule

Customer and driver mobile apps must never contain:

- payment provider private keys
- SMS provider keys
- backend admin credentials
- database credentials

The mobile apps should only call backend APIs and use short-lived user tokens issued by the backend.

## 8. Database and Migration Plan

### 8.1 Database Strategy

Use `Flyway` for all schema changes.

Rules:

- versioned SQL migrations only
- never edit historical migration files after production use
- repeatable migrations only for safe views and helper functions
- one source-controlled migration path for all environments

### 8.2 Migration File Structure

Recommended layout:

```text
src/main/resources/db/migration/
  V1__baseline_schema.sql
  V2__auth_and_customer_tables.sql
  V3__driver_and_booking_tables.sql
  V4__pricing_and_quote_tables.sql
  V5__dispatch_trip_finance_tables.sql
  V6__support_notification_audit.sql
  R__reporting_views.sql
```

### 8.3 Migration Principles

- all DDL must be backward-compatible when possible
- destructive changes require a multi-step rollout
- data migrations should be idempotent
- large backfills should run as controlled jobs, not inline in startup migration

### 8.4 Environment Migration Behavior

| Environment | Migration Mode |
|---|---|
| `local` | auto-run on app startup allowed |
| `dev` | auto-run in deploy pipeline allowed |
| `staging` | auto-run with pipeline visibility allowed |
| `production` | run as separate pre-deploy step with approval |

### 8.5 Production Migration Workflow

1. CI validates migrations on ephemeral PostgreSQL.
2. Release candidate is approved.
3. Production database snapshot is taken automatically before migration step.
4. Migration job runs once against production.
5. Health checks validate schema compatibility.
6. Application deployment proceeds.

### 8.6 Rollback Rule for Database Changes

Do not rely on destructive down-migrations in production.

Preferred strategy:

- application rollback to prior image when schema is backward-compatible
- forward-fix migration for bad logic
- point-in-time restore only for severe recovery scenarios

### 8.7 Seed Data Strategy

Seed safely by environment:

- local: demo cities, zones, pricing, test users
- dev: synthetic seed pack
- staging: approved masked or synthetic operational config
- production: bootstrap master data only, no fake users

## 9. CI/CD Strategy

### 9.1 Branch and Promotion Model

For a low-burn team, use a simple trunk-oriented model:

- feature branches from `main`
- pull request required for merge
- `main` remains releasable
- production release created from tagged commit

Optional release branch:

- release branches may be created for controlled cutovers or hotfix rehearsals
- do not make `develop` mandatory unless team size justifies it

### 9.2 CI Pipeline Stages

Every PR should run:

1. dependency restore
2. static checks
3. backend unit tests
4. backend integration tests
5. ArchUnit boundary tests
6. API contract validation if generated artifacts exist
7. Flyway migration validation on ephemeral PostgreSQL
8. build backend container image
9. build admin web app
10. basic mobile lint and build verification where practical

### 9.3 Recommended CI Tooling

- GitHub Actions for CI/CD
- Docker Buildx for image builds
- ECR for container storage
- artifact caching for Gradle and Node modules

### 9.4 CD Pipeline Stages

### Dev Deployment

- trigger on merge to `main`
- build immutable image tag from commit SHA
- run migrations automatically
- deploy API and worker
- run smoke tests

### Staging Deployment

- trigger automatically after dev success or manual promote
- deploy same image artifact that passed dev
- run staging smoke tests
- run core end-to-end validation for booking, assignment, trip start, payment, and admin login

### Production Deployment

- manual approval required
- deploy same tested image artifact
- run production migration step first
- perform rolling deploy
- run post-deploy smoke tests
- announce deployment in ops channel

### 9.5 Recommended GitHub Actions Workflow Set

| Workflow | Trigger | Purpose |
|---|---|---|
| `ci-backend.yml` | PR and push | test, lint, package backend |
| `ci-admin-web.yml` | PR and push | build and test admin web |
| `ci-mobile.yml` | PR and push | mobile lint and build checks |
| `deploy-dev.yml` | merge to main | deploy dev |
| `deploy-staging.yml` | manual or promote | deploy staging |
| `deploy-prod.yml` | tag or manual dispatch | deploy production |
| `db-migrate-prod.yml` | called by prod deploy | run Flyway migration |

### 9.6 Deployment Strategy

For backend:

- use rolling deployment for MVP
- minimum two API tasks in production if budget allows
- worker can start with one task in production and one in staging

For admin web:

- static deploy with cache-busting filenames
- invalidate CDN selectively on release

For mobile apps:

- internal builds distributed to QA and ops via TestFlight and Play Internal Testing
- production mobile releases promoted through store workflows
- backend must remain backward-compatible because store rollout is not instantaneous

### 9.7 Release Gates

Production release must not proceed unless:

- CI is green
- migrations validated
- smoke tests pass in staging
- no critical open incident affecting release area
- ops knows if pricing, payment, or notification behavior changed

### 9.8 Rollback Plan

### Backend

- redeploy previous known-good image
- keep database unchanged unless a severe incident requires restore

### Admin Web

- redeploy prior static bundle

### Mobile

- stop staged rollout if issue discovered
- use backend feature flags and compatibility handling to mitigate if app store rollback is slow

## 10. Container and Build Artifact Strategy

### 10.1 Backend Container

Use one backend image with:

- app jar
- runtime config entrypoint
- support for `api` and `worker` profile

Recommended tags:

- immutable SHA tag
- optional semver or release tag

### 10.2 Artifact Rules

- every deployment uses immutable artifact references
- production never deploys `latest`
- release notes should reference commit SHA, image tag, migration version, and operator

## 11. Logging Plan

### 11.1 Log Format

Use structured JSON logs in all non-local environments.

Required fields:

- timestamp
- level
- service
- environment
- request_id
- actor_type
- actor_id
- session_id
- booking_id when present
- trip_id when present
- assignment_id when present
- error_code when present

### 11.2 Logging Destinations

- stdout from containers
- centralized aggregation in CloudWatch Logs
- optional forwarding later to Datadog, ELK, or another analysis platform

### 11.3 Log Levels

Recommended defaults:

- `INFO` in production
- `DEBUG` only in local and controlled dev sessions
- package-level overrides for noisy frameworks

### 11.4 Sensitive Data Rules

Do not log:

- OTP values
- full mobile numbers
- passwords
- refresh tokens
- provider secrets
- raw KYC document payloads
- full payment payloads with sensitive metadata

Mask:

- phone numbers
- emails where required
- provider references if needed for support-safe logs

### 11.5 Log Retention

Suggested retention:

- dev: `14 days`
- staging: `30 days`
- production app logs: `30 to 90 days`
- audit logs are not log-retention only; they live in PostgreSQL per retention policy

## 12. Monitoring and Observability Plan

### 12.1 Technical Monitoring

Use:

- Spring Boot Actuator
- Micrometer
- OpenTelemetry traces
- CloudWatch metrics and alarms

Expose and track:

- JVM memory and GC
- API latency and status codes
- database connection pool health
- Redis latency
- worker queue or outbox lag
- scheduled job success and failure
- payment callback latency
- tracking ingest throughput

### 12.2 Business Monitoring

Create dashboards for:

- quote conversion
- booking creation rate
- fulfillment rate
- assignment latency
- on-time arrival rate
- rescue queue size
- trip completion rate
- payment failure rate
- refund rate
- support ticket volume

### 12.3 Core Dashboards

Minimum dashboards:

- `Platform Health`
- `Booking Funnel`
- `Dispatch and Rescue`
- `Payments and Refunds`
- `Tracking and Notifications`
- `Support and Incidents`

### 12.4 Health Endpoints

Use Spring Boot Actuator endpoints for:

- liveness
- readiness
- database connectivity
- Redis connectivity
- critical provider reachability where safe

Do not fail readiness because a non-critical downstream like SMS has a temporary issue, but do surface it in metrics and alerts.

### 12.5 Alerting Strategy

Alerts should go to:

- engineering on-call or designated release owner
- ops channel for business-critical workflow failures

Required alerts:

- booking API latency spike
- booking create failure spike
- assignment backlog above threshold
- rescue queue growth
- payment webhook failure or backlog
- notification delivery failure spike
- tracking ingestion failure spike
- outbox backlog growing or stuck
- database CPU, storage, or connection exhaustion risk

### 12.6 SLO Baseline

Initial MVP targets:

- customer booking APIs: `99.9%` monthly availability
- admin and ops console APIs: `99.5%` monthly availability
- critical background job success: `>= 99%`
- payment callback processing delay: target under `5 minutes`

## 13. Backup, Restore, and DR Plan

### 13.1 Backup Scope

Backups must cover:

- PostgreSQL database
- object storage buckets
- deployment manifests and infrastructure code
- audit-relevant exported reports if generated

Redis is not a system-of-record in MVP and does not require strict restore guarantees.

### 13.2 PostgreSQL Backup Policy

Production:

- automated daily snapshots
- point-in-time recovery enabled
- retention target `14 days` minimum for MVP
- manual snapshot before major releases and major migrations

Staging:

- daily snapshot
- retention target `7 days`

Dev:

- optional low-retention snapshots if cost-effective

### 13.3 Object Storage Backup Policy

For S3 buckets:

- enable versioning
- enable lifecycle policy
- retain KYC and evidence objects per compliance policy
- move old data to cheaper storage classes where safe

### 13.4 Restore Objectives

MVP recovery targets:

- production database `RPO <= 15 minutes`
- production database `RTO <= 4 hours`
- staging restore `RTO <= 1 business day`

These are practical MVP targets, not fintech-grade targets.

### 13.5 Restore Drill Policy

Perform restore drills:

- once before launch
- then at least quarterly

Drill output should verify:

- database restore works
- app can boot against restored data
- key booking and admin queries function
- object storage access for protected assets still works

### 13.6 Disaster Recovery Position

For MVP:

- use single-region active production
- rely on managed service durability and backups
- do not build multi-region active-active yet

Future upgrade path:

- cross-region snapshot copy
- read replica promotion plan
- warm standby environment for production

## 14. Security and Access Plan

### 14.1 IAM and Least Privilege

- separate IAM roles for API, worker, CI deployer, and admin humans
- no shared production credentials in chat or docs
- use role-based access, not long-lived shared users

### 14.2 Admin Access

- admin web behind authenticated login
- production admin roles limited by RBAC
- all admin actions audited
- 2FA for production admins should be treated as launch-required even if initial implementation is simple

### 14.3 Network Access

- PostgreSQL and Redis private only
- object storage bucket access restricted to service roles and signed URLs
- production console access logged and monitored

### 14.4 Security Scanning

CI should include:

- dependency vulnerability scan
- container image scan
- secret scan on repository content

## 15. Operational Runbooks

The following runbooks must exist before production launch:

- backend deploy and rollback
- database migration runbook
- payment callback incident runbook
- assignment backlog runbook
- tracking outage runbook
- notification provider outage runbook
- restore and recovery runbook
- admin account lockout runbook

## 16. Cost-Control Plan

Because capital is constrained, infrastructure decisions must stay disciplined.

### 16.1 Low-Burn MVP Controls

- use managed services instead of building internal platforms
- keep one primary backend codebase and one database
- avoid preview environments for every branch
- start with modest instance sizes and autoscale only on proven need
- use lifecycle policies for logs and storage
- keep staging smaller than production

### 16.2 Spend Review Cadence

Review monthly:

- compute cost
- database cost
- SMS and push cost
- object storage growth
- monitoring and log cost

Track cost drivers by:

- environment
- service
- provider

## 17. Launch Readiness Checklist

Before MVP launch, confirm:

- all four environments exist and are documented
- production deploy requires approval
- rollback procedure tested
- Flyway migrations tested end to end
- production backups and PITR enabled
- restore drill completed successfully
- centralized logging is active
- dashboards and alerts are active
- secrets are managed only through approved secret stores
- admin access and audit trails are functioning
- mobile apps point to correct production endpoints

## 18. Recommended Next Step

The next useful document after this infra plan is an `Ops_SOP.md` or `Implementation_Backlog.md` so the platform, backend, ops, and release work can be converted into owned tasks and launch checklists.

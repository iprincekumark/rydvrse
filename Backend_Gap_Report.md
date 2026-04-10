# Backend Gap Report

## Purpose

This document compares the current backend implementation in `rydvrse-server/` against:

- `API_Spec.md`
- `Low_Level_Design.md`

The goal is to show:

- what is already implemented
- what was added in the latest module-completion pass
- what still remains as a deliberate or unresolved gap

This report reflects the backend codebase as of `2026-04-11`.

## Summary

The backend is no longer missing dedicated implementations for the previously thin modules:

- `dispatch`
- `tracking`
- `notification`
- `reporting`

These modules now exist with concrete:

- controllers
- services
- repositories
- scheduled jobs
- tests

The codebase also still passes the Maven test suite from `rydvrse-server/`.

## Comparison Outcome

### 1. Dispatch

#### Previously Missing or Thin

- no dedicated dispatch controllers
- no assignment attempt entity or repository
- no driver candidate snapshot persistence
- no dispatch-specific jobs
- rescue and reassignment logic lived only in broader admin/trip services

#### Now Implemented

- `dispatch.api.DriverAssignmentController`
- `dispatch.api.AssignmentRescueController`
- `dispatch.application.DispatchOrchestrator`
- `dispatch.application.CandidateDiscoveryService`
- `dispatch.application.CandidateScoringService`
- `dispatch.application.AssignmentQueryService`
- `dispatch.application.AssignmentOfferService`
- `dispatch.application.AssignmentLockService`
- `dispatch.application.RescueQueueService`
- `dispatch.application.ReassignmentService`
- `dispatch.domain.AssignmentAttemptEntity`
- `dispatch.domain.DriverCandidateSnapshotEntity`
- `dispatch.infrastructure.AssignmentAttemptRepository`
- `dispatch.infrastructure.DriverCandidateSnapshotRepository`
- `dispatch.jobs.AssignmentOfferExpiryJob`
- `dispatch.jobs.AssignmentRescueScannerJob`

#### Remaining Gaps

- candidate scoring is deterministic and lightweight, not geo-optimized
- offer expiry flags rescue, but does not yet auto-rotate to the next ranked candidate
- no dedicated `DriverEligibilityFacade` abstraction yet
- no advanced distance or route-time estimation during candidate ranking

### 2. Tracking

#### Previously Missing or Thin

- no dedicated tracking package implementation
- tracking lived inside the trip service
- no stale-session job
- no projection/cache layer
- no dedicated location ingestion controller

#### Now Implemented

- `tracking.api.TrackingController`
- `tracking.api.TrackingIngestionController`
- `tracking.application.TrackingSessionService`
- `tracking.application.LocationIngestionService`
- `tracking.application.TrackingProjectionService`
- `tracking.application.TrackingQueryService`
- `tracking.application.TrackingStreamService`
- `tracking.application.TripShareLinkService`
- `tracking.application.EtaEstimationService`
- `tracking.jobs.TrackingSessionStaleCheckJob`

#### Remaining Gaps

- route polyline generation is still placeholder
- ETA estimation is heuristic, not map-provider based
- SSE stream is in-memory and single-node friendly, not horizontally shared
- no public share-link consumption endpoint yet

### 3. Notification

#### Previously Missing or Thin

- notification package existed but had no code
- no notification entities or repositories
- no planner, renderer, or delivery services
- no notification worker jobs
- outbox events were not being consumed into deliveries

#### Now Implemented

- `notification.domain.NotificationTemplateEntity`
- `notification.domain.NotificationEventEntity`
- `notification.domain.NotificationDeliveryEntity`
- `notification.infrastructure.NotificationTemplateRepository`
- `notification.infrastructure.NotificationEventRepository`
- `notification.infrastructure.NotificationDeliveryRepository`
- `notification.application.NotificationPlannerService`
- `notification.application.TemplateRenderService`
- `notification.application.NotificationPreferenceService`
- `notification.application.PushNotificationService`
- `notification.application.SmsNotificationService`
- `notification.application.NotificationDeliveryService`
- `notification.jobs.NotificationDispatchJob`
- `notification.jobs.NotificationRetryJob`
- `common.outbox.OutboxDispatcherJob`

#### Remaining Gaps

- push and SMS senders are backend-safe stubs, not real provider integrations yet
- no provider callback ingestion for delivery receipts yet
- preference handling is event-type based, not customer-configurable
- no admin notification-template management API yet

### 4. Reporting

#### Previously Missing or Thin

- reporting package existed but had no code
- no reporting API
- no rollup job
- no analytics query layer

#### Now Implemented

- `reporting.api.AdminReportingController`
- `reporting.application.OperationsReportingService`
- `reporting.application.ReportingRollupJob`
- `reporting.infrastructure.ReportingJdbcRepository`

#### Remaining Gaps

- reporting is daily-rollup focused, not near-real-time
- report surface is currently limited to operations summary
- no drill-down report endpoints by zone, driver cohort, or payout cohort yet

## API Spec Alignment

### Now Covered by Dedicated Module Endpoints

- `GET /api/v1/drivers/assignments/offers`
- `GET /api/v1/drivers/assignments/{assignment_id}`
- `POST /api/v1/drivers/assignments/{assignment_id}/accept`
- `POST /api/v1/drivers/assignments/{assignment_id}/decline`
- `GET /api/v1/trips/{trip_id}/tracking`
- `GET /api/v1/trips/{trip_id}/tracking/stream`
- `POST /api/v1/drivers/trips/{trip_id}/location-pings`
- `POST /api/v1/trips/{trip_id}/share-links`
- `GET /api/v1/admin/bookings/rescue-queue`
- `GET /api/v1/admin/bookings/{booking_id}/assignment-candidates`
- `POST /api/v1/admin/bookings/{booking_id}/reassign`
- `GET /api/v1/admin/reports/operations`

### Still Partially Aligned

- notification behavior is present internally but not exposed through extra admin APIs
- reporting matches the MVP route surface, but not the full analytical depth implied by later-phase LLD expectations

## Low-Level Design Alignment

### Closed Gaps

- dispatch module now has dedicated adapters, services, repositories, and jobs
- tracking module now has dedicated adapters, services, repositories, and a stale-session job
- notification module now has its own persistence model and async worker flow
- reporting now has a read layer and a rollup job
- outbox event processing now has an execution path

### Remaining Architectural Gaps

- some LLD-named facades and policies are still represented by direct service logic instead of explicit interface layers
- async processing still runs in the app scheduler and database, not in a separate worker process
- distributed emitter/state coordination is not yet implemented for multi-instance live tracking
- payment reconciliation, support escalation, and document expiry jobs remain outside this specific completion pass

## Test Coverage Added in This Pass

- `dispatch.api.DriverAssignmentControllerTest`
- `dispatch.application.DispatchOrchestratorTest`
- `tracking.api.TrackingControllerTest`
- `tracking.application.TrackingSessionServiceTest`
- `notification.application.NotificationPlannerServiceTest`
- `reporting.application.OperationsReportingServiceTest`

## Recommended Next Backend Steps

1. replace stub push/SMS implementations with real provider adapters
2. add automatic next-candidate offer rotation after expiry
3. add distributed live-tracking streaming support for multi-instance deployment
4. deepen reporting with zone and SLA drill-down endpoints
5. add integration tests for outbox, notification, and reporting SQL paths

## Conclusion

The highest-priority structural gaps identified against `API_Spec.md` and `Low_Level_Design.md` have now been addressed.

The backend still has remaining hardening work, but it is no longer missing entire module implementations for dispatch, tracking, notification, and reporting.

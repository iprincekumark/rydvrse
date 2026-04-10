package com.rydvrse.driver.application;

import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.driver.domain.DriverAvailabilityStatusEntity;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverAvailabilityStatusRepository;
import com.rydvrse.finance.domain.DriverEarningLedgerEntity;
import com.rydvrse.finance.infrastructure.DriverEarningLedgerRepository;
import com.rydvrse.trip.infrastructure.TripRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DriverDashboardQueryService {

    private final DriverProfileService driverProfileService;
    private final DriverAvailabilityStatusRepository availabilityStatusRepository;
    private final AssignmentRepository assignmentRepository;
    private final DriverEarningLedgerRepository driverEarningLedgerRepository;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;

    public DriverDashboardQueryService(
            DriverProfileService driverProfileService,
            DriverAvailabilityStatusRepository availabilityStatusRepository,
            AssignmentRepository assignmentRepository,
            DriverEarningLedgerRepository driverEarningLedgerRepository,
            BookingRepository bookingRepository,
            TripRepository tripRepository
    ) {
        this.driverProfileService = driverProfileService;
        this.availabilityStatusRepository = availabilityStatusRepository;
        this.assignmentRepository = assignmentRepository;
        this.driverEarningLedgerRepository = driverEarningLedgerRepository;
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
    }

    public Map<String, Object> getDashboard() {
        DriverProfileEntity profile = driverProfileService.requireCurrentProfile();
        DriverAvailabilityStatusEntity availability = availabilityStatusRepository.findById(profile.getId()).orElse(null);
        long todayEarnings = driverEarningLedgerRepository.sumNetPayoutPaiseByDriverProfileId(profile.getId()).orElse(0L);
        return Map.of(
                "profile_summary", driverProfileService.toResponse(profile),
                "current_availability", availability == null ? "OFFLINE" : availability.getCurrentStatus(),
                "active_assignment", null,
                "upcoming_jobs", List.of(),
                "todays_earnings", Map.of("amount_paise", todayEarnings, "currency", "INR"),
                "active_incentive_summary", Map.of("amount_paise", 0, "currency", "INR"),
                "onboarding_blockers", "APPROVED".equals(profile.getOnboardingStatus()) ? List.of() : List.of("Complete onboarding review")
        );
    }

    public List<Map<String, Object>> upcomingJobs() {
        UUID driverId = driverProfileService.requireCurrentProfile().getId();
        return tripRepository.findByDriverProfileIdOrderByCreatedAtDesc(driverId).stream()
                .filter(trip -> !List.of("COMPLETED", "ABANDONED").contains(trip.getStatus()))
                .map(trip -> bookingRepository.findById(trip.getBookingId()).map(booking -> Map.<String, Object>of(
                        "trip_id", trip.getId(),
                        "booking_id", booking.getId(),
                        "service_type", booking.getServiceType(),
                        "scheduled_pickup_at", booking.getScheduledPickupAt(),
                        "pickup_address", booking.getPickupAddressText(),
                        "drop_address", booking.getDropAddressText(),
                        "trip_state", trip.getStatus()
                )).orElse(Map.of(
                        "trip_id", trip.getId(),
                        "booking_id", trip.getBookingId(),
                        "trip_state", trip.getStatus()
                )))
                .toList();
    }

    public Map<String, Object> earningsSummary(String period, LocalDate from, LocalDate to) {
        DriverProfileEntity profile = driverProfileService.requireCurrentProfile();
        OffsetDateTime[] window = resolveWindow(period, from, to);
        long total = driverEarningLedgerRepository.sumNetPayoutPaiseByDriverProfileIdAndCreatedAtBetween(profile.getId(), window[0], window[1]).orElse(0L);
        long pending = driverEarningLedgerRepository.sumPendingByDriverProfileId(profile.getId()).orElse(0L);
        long jobs = driverEarningLedgerRepository.countByDriverProfileIdAndCreatedAtBetween(profile.getId(), window[0], window[1]);
        return Map.of(
                "period", period == null ? "THIS_WEEK" : period,
                "total_completed_earnings", Map.of("amount_paise", total, "currency", "INR"),
                "pending_settlement", Map.of("amount_paise", pending, "currency", "INR"),
                "completed_jobs_count", jobs,
                "incentive_totals", Map.of("amount_paise", 0, "currency", "INR"),
                "deductions", Map.of("amount_paise", 0, "currency", "INR")
        );
    }

    public List<Map<String, Object>> earningsLedger() {
        UUID driverId = driverProfileService.requireCurrentProfile().getId();
        return driverEarningLedgerRepository.findByDriverProfileIdOrderByCreatedAtDesc(driverId).stream()
                .map(this::toLedgerRow)
                .toList();
    }

    private Map<String, Object> toLedgerRow(DriverEarningLedgerEntity ledger) {
        return Map.of(
                "ledger_id", ledger.getId(),
                "trip_id", ledger.getTripId(),
                "assignment_id", ledger.getAssignmentId(),
                "state", ledger.getLedgerStatus(),
                "gross_payout_paise", ledger.getGrossPayoutPaise(),
                "adjustment_paise", ledger.getAdjustmentPaise(),
                "net_payout_paise", ledger.getNetPayoutPaise(),
                "locked_at", ledger.getLockedAt(),
                "settled_at", ledger.getSettledAt(),
                "components", ledger.getSnapshotPayload()
        );
    }

    private OffsetDateTime[] resolveWindow(String period, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        if ("TODAY".equalsIgnoreCase(period)) {
            return new OffsetDateTime[]{today.atStartOfDay().atOffset(java.time.ZoneOffset.ofHoursMinutes(5, 30)), today.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.ofHoursMinutes(5, 30))};
        }
        if ("THIS_MONTH".equalsIgnoreCase(period)) {
            LocalDate start = today.withDayOfMonth(1);
            return new OffsetDateTime[]{start.atStartOfDay().atOffset(java.time.ZoneOffset.ofHoursMinutes(5, 30)), start.plusMonths(1).atStartOfDay().atOffset(java.time.ZoneOffset.ofHoursMinutes(5, 30))};
        }
        if ("CUSTOM".equalsIgnoreCase(period) && from != null && to != null) {
            return new OffsetDateTime[]{from.atStartOfDay().atOffset(java.time.ZoneOffset.ofHoursMinutes(5, 30)), to.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.ofHoursMinutes(5, 30))};
        }
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        return new OffsetDateTime[]{weekStart.atStartOfDay().atOffset(java.time.ZoneOffset.ofHoursMinutes(5, 30)), weekStart.plusDays(7).atStartOfDay().atOffset(java.time.ZoneOffset.ofHoursMinutes(5, 30))};
    }
}

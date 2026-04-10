package com.rydvrse.admin.application;

import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import com.rydvrse.support.infrastructure.SupportTicketRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class DashboardQueryService {

    private final BookingRepository bookingRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final DriverProfileRepository driverProfileRepository;

    public DashboardQueryService(
            BookingRepository bookingRepository,
            SupportTicketRepository supportTicketRepository,
            DriverProfileRepository driverProfileRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.driverProfileRepository = driverProfileRepository;
    }

    public Map<String, Object> summary() {
        long atRiskCount = bookingRepository.findByStatusInAndScheduledPickupAtBetween(
                java.util.List.of("PENDING_ASSIGNMENT", "ASSIGNED"),
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2)
        ).stream().filter(booking -> "PENDING_ASSIGNMENT".equals(booking.getStatus())).count();
        return Map.of(
                "bookings_by_state", Map.of(
                        "pending_assignment", bookingRepository.findByStatusInAndScheduledPickupAtBetween(java.util.List.of("PENDING_ASSIGNMENT"), OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1)).size(),
                        "assigned", bookingRepository.findByStatusInAndScheduledPickupAtBetween(java.util.List.of("ASSIGNED"), OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1)).size(),
                        "completed", bookingRepository.findByStatusInAndScheduledPickupAtBetween(java.util.List.of("COMPLETED", "COMPLETED_PAYMENT_PENDING"), OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1)).size()
                ),
                "at_risk_count", atRiskCount,
                "unassigned_count", bookingRepository.findByStatusInAndScheduledPickupAtBetween(java.util.List.of("PENDING_ASSIGNMENT"), OffsetDateTime.now(), OffsetDateTime.now().plusDays(1)).size(),
                "average_assignment_time_seconds", 0,
                "support_ticket_open_count", supportTicketRepository.countByStatusIn(java.util.List.of("OPEN", "IN_PROGRESS", "PENDING_INTERNAL", "REOPENED")),
                "refund_count", 0,
                "driver_onboarding_pending_count", driverProfileRepository.findAll().stream().filter(profile -> java.util.List.of("SUBMITTED", "IN_REVIEW", "CORRECTION_REQUIRED").contains(profile.getOnboardingStatus())).count()
        );
    }
}

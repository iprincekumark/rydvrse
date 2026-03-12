package com.rydvrse.operations.service;

import com.rydvrse.driver.service.DriverService;
import com.rydvrse.shared.enums.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Operations service — internal operational workflows.
 * Handles: document verification, dispute resolution, quality audits.
 * Used by admin/operations team to manage platform quality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationsService {

    private final DriverService driverService;

    /** Verify a driver after document review */
    @Transactional
    public void approveDriver(UUID driverId) {
        driverService.verifyDriver(driverId);
        log.info("[OPS] Driver {} approved and activated", driverId);
    }

    /** Reject driver verification with reason */
    @Transactional
    public void rejectDriver(UUID driverId, String reason) {
        log.info("[OPS] Driver {} rejected: {}", driverId, reason);
        // In production: update driver status to REJECTED, notify driver
    }

    /** Handle dispute between customer and driver */
    @Transactional
    public void resolveDispute(UUID tripId, String resolution) {
        log.info("[OPS] Dispute for trip {} resolved: {}", tripId, resolution);
        // In production: update trip, refund if needed, notify parties
    }
}

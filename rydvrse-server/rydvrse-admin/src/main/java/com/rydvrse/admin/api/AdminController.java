package com.rydvrse.admin.api;

import com.rydvrse.driver.repository.DriverRepository;
import com.rydvrse.operations.service.OperationsService;
import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Admin controller — internal dashboard APIs.
 * Restricted to ADMIN and SUPER_ADMIN roles.
 */
@RestController
@RequestMapping("/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final OperationsService operationsService;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = Map.of(
                "totalDrivers", driverRepository.count(),
                "totalTrips", tripRepository.count()
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/drivers/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveDriver(@PathVariable UUID id) {
        operationsService.approveDriver(id);
        return ResponseEntity.ok(ApiResponse.success("Driver approved", null));
    }

    @PostMapping("/drivers/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectDriver(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        operationsService.rejectDriver(id, body.get("reason"));
        return ResponseEntity.ok(ApiResponse.success("Driver rejected", null));
    }

    @PostMapping("/disputes/{tripId}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveDispute(
            @PathVariable UUID tripId, @RequestBody Map<String, String> body) {
        operationsService.resolveDispute(tripId, body.get("resolution"));
        return ResponseEntity.ok(ApiResponse.success("Dispute resolved", null));
    }
}

package com.rydvrse.admin.controller;

import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/api/v1/admin") @RequiredArgsConstructor
@Tag(name = "Admin", description = "Platform administration and monitoring")
public class AdminController {

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Platform metrics dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboardStats() {
        Map<String, Object> stats = Map.of(
                "realTime", Map.of("activeTrips", 0, "onlineDrivers", 0, "searchingTrips", 0, "activeSosAlerts", 0),
                "today", Map.of("totalTrips", 0, "completedTrips", 0, "cancelledTrips", 0, "revenue", 0.0, "newCustomers", 0, "newDrivers", 0),
                "driverVerification", Map.of("pendingReview", 0, "approvedToday", 0, "rejectedToday", 0),
                "safety", Map.of("openIncidents", 0, "unresolvedSos", 0, "routeDeviationsToday", 0)
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/customers")
    @Operation(summary = "List customers")
    public ResponseEntity<ApiResponse<Void>> listCustomers() {
        return ResponseEntity.ok(ApiResponse.success("List customers endpoint", null));
    }

    @GetMapping("/drivers")
    @Operation(summary = "List drivers")
    public ResponseEntity<ApiResponse<Void>> listDrivers() {
        return ResponseEntity.ok(ApiResponse.success("List drivers endpoint", null));
    }

    @GetMapping("/trips")
    @Operation(summary = "List trips")
    public ResponseEntity<ApiResponse<Void>> listTrips() {
        return ResponseEntity.ok(ApiResponse.success("List trips endpoint", null));
    }

    @GetMapping("/payments")
    @Operation(summary = "Transaction list")
    public ResponseEntity<ApiResponse<Void>> listPayments() {
        return ResponseEntity.ok(ApiResponse.success("List payments endpoint", null));
    }

    @GetMapping("/safety/sos")
    @Operation(summary = "SOS alerts")
    public ResponseEntity<ApiResponse<Void>> listSos() {
        return ResponseEntity.ok(ApiResponse.success("List SOS alerts endpoint", null));
    }

    @GetMapping("/safety/incidents")
    @Operation(summary = "Incident queue")
    public ResponseEntity<ApiResponse<Void>> listIncidents() {
        return ResponseEntity.ok(ApiResponse.success("List incidents endpoint", null));
    }

    @GetMapping("/fare-rules")
    @Operation(summary = "List fare rules")
    public ResponseEntity<ApiResponse<Void>> listFareRules() {
        return ResponseEntity.ok(ApiResponse.success("List fare rules endpoint", null));
    }
}

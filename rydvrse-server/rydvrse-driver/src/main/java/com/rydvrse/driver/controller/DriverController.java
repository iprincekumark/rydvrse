package com.rydvrse.driver.controller;

import com.rydvrse.driver.entity.*;
import com.rydvrse.driver.service.DriverService;
import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver", description = "Driver profile, documents, availability, and location")
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/me")
    @Operation(summary = "Get own profile")
    public ResponseEntity<ApiResponse<Driver>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(driverService.getProfile(principal.getUserId())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile")
    public ResponseEntity<ApiResponse<Driver>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> updates) {
        Driver driver = driverService.updateProfile(principal.getUserId(),
                updates.get("name"), updates.get("email"), updates.get("profileImageUrl"));
        return ResponseEntity.ok(ApiResponse.success(driver));
    }

    @PostMapping("/me/documents")
    @Operation(summary = "Upload KYC document")
    public ResponseEntity<ApiResponse<DriverDocument>> uploadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody DriverDocument document) {
        return ResponseEntity.ok(ApiResponse.success(driverService.uploadDocument(principal.getUserId(), document)));
    }

    @GetMapping("/me/documents")
    @Operation(summary = "List own documents")
    public ResponseEntity<ApiResponse<List<DriverDocument>>> getDocuments(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(driverService.getDocuments(principal.getUserId())));
    }

    @PutMapping("/me/availability")
    @Operation(summary = "Go online/offline")
    public ResponseEntity<ApiResponse<DriverAvailability>> updateAvailability(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(ApiResponse.success(
                driverService.updateAvailability(principal.getUserId(), body.getOrDefault("isOnline", false))));
    }

    @PutMapping("/me/location")
    @Operation(summary = "Update GPS location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Double> body) {
        driverService.updateLocation(principal.getUserId(),
                body.get("lat"), body.get("lng"), body.get("heading"), body.get("speed"));
        return ResponseEntity.ok(ApiResponse.success("Location updated", null));
    }
}

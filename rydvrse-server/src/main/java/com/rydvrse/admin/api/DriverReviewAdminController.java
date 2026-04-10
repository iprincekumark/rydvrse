package com.rydvrse.admin.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.driver.application.DriverReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/drivers")
public class DriverReviewAdminController {

    private final DriverReviewService driverReviewService;

    public DriverReviewAdminController(DriverReviewService driverReviewService) {
        this.driverReviewService = driverReviewService;
    }

    @GetMapping("/onboarding-queue")
    public ApiResponse<List<Map<String, Object>>> onboardingQueue() {
        return ApiResponse.of(driverReviewService.onboardingQueue(), null);
    }

    @GetMapping("/{driverId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable UUID driverId) {
        return ApiResponse.of(driverReviewService.detail(driverId), null);
    }

    @PostMapping("/{driverId}/approve")
    public ApiResponse<Map<String, Object>> approve(@PathVariable UUID driverId, @Valid @RequestBody ReviewNoteRequest request) {
        return ApiResponse.of(driverReviewService.approve(driverId, request.reasonNote(), request.rowVersion()), null);
    }

    @PostMapping("/{driverId}/reject")
    public ApiResponse<Map<String, Object>> reject(@PathVariable UUID driverId, @Valid @RequestBody RejectDriverRequest request) {
        return ApiResponse.of(driverReviewService.reject(driverId, request.reasonCode(), request.reasonNote(), request.rowVersion()), null);
    }

    @PostMapping("/{driverId}/request-correction")
    public ApiResponse<Map<String, Object>> requestCorrection(@PathVariable UUID driverId, @Valid @RequestBody CorrectionRequest request) {
        return ApiResponse.of(driverReviewService.requestCorrection(driverId, request.requiredCorrections(), request.reasonNote(), request.rowVersion()), null);
    }

    @PostMapping("/{driverId}/suspend")
    public ApiResponse<Map<String, Object>> suspend(@PathVariable UUID driverId, @Valid @RequestBody SuspendDriverRequest request) {
        return ApiResponse.of(driverReviewService.suspend(driverId, request.reasonCode(), request.effectiveUntil(), request.reasonNote(), request.rowVersion()), null);
    }

    public record ReviewNoteRequest(String reasonNote, Long rowVersion) {
    }

    public record RejectDriverRequest(@NotBlank String reasonCode, String reasonNote, Long rowVersion) {
    }

    public record CorrectionRequest(@NotNull List<String> requiredCorrections, String reasonNote, Long rowVersion) {
    }

    public record SuspendDriverRequest(@NotBlank String reasonCode, OffsetDateTime effectiveUntil, String reasonNote, Long rowVersion) {
    }
}

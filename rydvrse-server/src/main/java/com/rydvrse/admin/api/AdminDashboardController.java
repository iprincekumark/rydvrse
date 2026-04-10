package com.rydvrse.admin.api;

import com.rydvrse.admin.application.AdminBookingService;
import com.rydvrse.admin.application.DashboardQueryService;
import com.rydvrse.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminDashboardController {

    private final DashboardQueryService dashboardQueryService;
    private final AdminBookingService adminBookingService;

    public AdminDashboardController(DashboardQueryService dashboardQueryService, AdminBookingService adminBookingService) {
        this.dashboardQueryService = dashboardQueryService;
        this.adminBookingService = adminBookingService;
    }

    @GetMapping("/dashboard/summary")
    public ApiResponse<Map<String, Object>> dashboardSummary() {
        return ApiResponse.of(dashboardQueryService.summary(), null);
    }

    @GetMapping("/bookings")
    public ApiResponse<List<Map<String, Object>>> bookings() {
        return ApiResponse.of(adminBookingService.list(), null);
    }

    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<Map<String, Object>> bookingDetail(@PathVariable UUID bookingId) {
        return ApiResponse.of(adminBookingService.detail(bookingId), null);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ApiResponse<Map<String, Object>> cancel(
            @PathVariable UUID bookingId,
            @Valid @RequestBody AdminCancelRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(adminBookingService.adminCancel(
                bookingId,
                new AdminBookingService.AdminCancelCommand(
                        request.reasonCode(),
                        request.reasonNote(),
                        request.refundRecommendation(),
                        request.rowVersion()
                ),
                idempotencyKey
        ), null);
    }

    public record AdminCancelRequest(
            @jakarta.validation.constraints.NotBlank String reasonCode,
            String reasonNote,
            String refundRecommendation,
            @NotNull Long rowVersion
    ) {
    }
}

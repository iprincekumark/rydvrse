package com.rydvrse.admin.api;

import com.rydvrse.admin.application.AdminCommercialService;
import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.finance.application.FinanceOperationsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCommercialController {

    private final AdminCommercialService adminCommercialService;
    private final FinanceOperationsService financeOperationsService;

    public AdminCommercialController(AdminCommercialService adminCommercialService, FinanceOperationsService financeOperationsService) {
        this.adminCommercialService = adminCommercialService;
        this.financeOperationsService = financeOperationsService;
    }

    @GetMapping("/pricing/plans")
    public ApiResponse<List<Map<String, Object>>> pricingPlans() {
        return ApiResponse.of(adminCommercialService.pricingPlans(), null);
    }

    @PostMapping("/pricing/plans")
    public ApiResponse<Map<String, Object>> createPricingPlan(@Valid @RequestBody CreatePricingPlanRequest request) {
        return ApiResponse.of(adminCommercialService.createPricingPlan(
                new AdminCommercialService.CreatePricingPlanCommand(
                        request.cityId(),
                        request.name(),
                        request.effectiveFrom(),
                        request.rules().stream().map(rule -> new AdminCommercialService.RuleInput(
                                rule.serviceType(),
                                rule.serviceZoneId(),
                                rule.airportZoneBandId(),
                                rule.oneWayBandId(),
                                rule.leadTimeBucket(),
                                rule.ruleType(),
                                rule.amountPaise()
                        )).toList(),
                        request.taxProfileVersion()
                )
        ), null);
    }

    @GetMapping("/pricing/plans/{planId}")
    public ApiResponse<Map<String, Object>> pricingPlan(@PathVariable UUID planId) {
        return ApiResponse.of(adminCommercialService.pricingPlan(planId), null);
    }

    @PostMapping("/pricing/plans/{planId}/publish")
    public ApiResponse<Map<String, Object>> publishPricingPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody PublishPlanRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(adminCommercialService.publishPricingPlan(planId, request.effectiveFrom(), request.reasonNote(), idempotencyKey), null);
    }

    @GetMapping("/payout/plans")
    public ApiResponse<List<Map<String, Object>>> payoutPlans() {
        return ApiResponse.of(adminCommercialService.payoutPlans(), null);
    }

    @PostMapping("/payout/plans")
    public ApiResponse<Map<String, Object>> createPayoutPlan(@Valid @RequestBody CreatePayoutPlanRequest request) {
        return ApiResponse.of(adminCommercialService.createPayoutPlan(
                new AdminCommercialService.CreatePayoutPlanCommand(
                        request.cityId(),
                        request.name(),
                        request.effectiveFrom(),
                        request.rules().stream().map(rule -> new AdminCommercialService.RuleInput(
                                rule.serviceType(),
                                rule.serviceZoneId(),
                                rule.airportZoneBandId(),
                                rule.oneWayBandId(),
                                rule.leadTimeBucket(),
                                rule.ruleType(),
                                rule.amountPaise()
                        )).toList()
                )
        ), null);
    }

    @PostMapping("/payout/plans/{planId}/publish")
    public ApiResponse<Map<String, Object>> publishPayoutPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody PublishPlanRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(adminCommercialService.publishPayoutPlan(planId, request.effectiveFrom(), request.reasonNote(), idempotencyKey), null);
    }

    @GetMapping("/serviceability/zones")
    public ApiResponse<List<Map<String, Object>>> serviceabilityZones() {
        return ApiResponse.of(adminCommercialService.serviceabilityZones(), null);
    }

    @PutMapping("/serviceability/zones/{zoneId}")
    public ApiResponse<Map<String, Object>> updateZone(@PathVariable UUID zoneId, @Valid @RequestBody UpdateZoneRequest request) {
        return ApiResponse.of(adminCommercialService.updateZone(
                zoneId,
                new AdminCommercialService.UpdateZoneCommand(request.status(), request.serviceTypesEnabled(), request.reasonNote(), request.rowVersion())
        ), null);
    }

    @GetMapping("/audit/logs")
    public ApiResponse<List<Map<String, Object>>> auditLogs() {
        return ApiResponse.of(adminCommercialService.auditLogs(), null);
    }

    @PostMapping("/refunds")
    public ApiResponse<Map<String, Object>> createRefund(
            @Valid @RequestBody CreateRefundRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(financeOperationsService.createRefund(
                new FinanceOperationsService.AdminRefundCommand(
                        request.bookingId(),
                        request.paymentId(),
                        request.amountPaise(),
                        request.reasonCode(),
                        request.reasonNote(),
                        request.rowVersion()
                ),
                idempotencyKey
        ), null);
    }

    @GetMapping("/refunds")
    public ApiResponse<List<Map<String, Object>>> refunds() {
        return ApiResponse.of(financeOperationsService.listRefunds(), null);
    }

    @GetMapping("/refunds/{refundId}")
    public ApiResponse<Map<String, Object>> refund(@PathVariable UUID refundId) {
        return ApiResponse.of(financeOperationsService.refundDetail(refundId), null);
    }

    public record RuleRequest(
            @NotBlank String serviceType,
            UUID serviceZoneId,
            UUID airportZoneBandId,
            UUID oneWayBandId,
            String leadTimeBucket,
            @NotBlank String ruleType,
            long amountPaise
    ) {
    }

    public record CreatePricingPlanRequest(
            @NotNull UUID cityId,
            @NotBlank String name,
            @NotNull OffsetDateTime effectiveFrom,
            @NotEmpty List<RuleRequest> rules,
            String taxProfileVersion
    ) {
    }

    public record CreatePayoutPlanRequest(
            @NotNull UUID cityId,
            @NotBlank String name,
            @NotNull OffsetDateTime effectiveFrom,
            @NotEmpty List<RuleRequest> rules
    ) {
    }

    public record PublishPlanRequest(@NotNull OffsetDateTime effectiveFrom, String reasonNote) {
    }

    public record UpdateZoneRequest(@NotBlank String status, @NotEmpty List<String> serviceTypesEnabled, String reasonNote, @NotNull Long rowVersion) {
    }

    public record CreateRefundRequest(
            @NotNull UUID bookingId,
            @NotNull UUID paymentId,
            long amountPaise,
            @NotBlank String reasonCode,
            String reasonNote,
            @NotNull Long rowVersion
    ) {
    }
}

package com.rydvrse.dispatch.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.dispatch.application.AssignmentLockService;
import com.rydvrse.dispatch.application.AssignmentOfferService;
import com.rydvrse.dispatch.application.AssignmentQueryService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class DriverAssignmentController {

    private final AssignmentQueryService assignmentQueryService;
    private final AssignmentOfferService assignmentOfferService;
    private final AssignmentLockService assignmentLockService;

    public DriverAssignmentController(
            AssignmentQueryService assignmentQueryService,
            AssignmentOfferService assignmentOfferService,
            AssignmentLockService assignmentLockService
    ) {
        this.assignmentQueryService = assignmentQueryService;
        this.assignmentOfferService = assignmentOfferService;
        this.assignmentLockService = assignmentLockService;
    }

    @GetMapping("/api/v1/drivers/assignments/offers")
    public ApiResponse<List<Map<String, Object>>> offers() {
        return ApiResponse.of(assignmentQueryService.offersForCurrentDriver(), null);
    }

    @GetMapping("/api/v1/drivers/assignments/{assignmentId}")
    public ApiResponse<Map<String, Object>> assignmentDetail(@PathVariable UUID assignmentId) {
        return ApiResponse.of(assignmentQueryService.assignmentDetail(assignmentId), null);
    }

    @PostMapping("/api/v1/drivers/assignments/{assignmentId}/accept")
    public ApiResponse<Map<String, Object>> accept(
            @PathVariable UUID assignmentId,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(assignmentLockService.acceptOffer(assignmentId, idempotencyKey), null);
    }

    @PostMapping("/api/v1/drivers/assignments/{assignmentId}/decline")
    public ApiResponse<Map<String, Object>> decline(
            @PathVariable UUID assignmentId,
            @RequestBody DeclineAssignmentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(
                assignmentOfferService.declineOffer(assignmentId, request.reasonCode(), request.reasonNote(), idempotencyKey),
                null
        );
    }

    public record DeclineAssignmentRequest(@NotBlank String reasonCode, String reasonNote) {
    }
}

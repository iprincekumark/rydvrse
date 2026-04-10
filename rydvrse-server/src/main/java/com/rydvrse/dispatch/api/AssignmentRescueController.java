package com.rydvrse.dispatch.api;

import com.rydvrse.admin.application.AdminBookingService;
import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.dispatch.application.ReassignmentService;
import com.rydvrse.dispatch.application.RescueQueueService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/v1/admin/bookings")
public class AssignmentRescueController {

    private final RescueQueueService rescueQueueService;
    private final ReassignmentService reassignmentService;

    public AssignmentRescueController(RescueQueueService rescueQueueService, ReassignmentService reassignmentService) {
        this.rescueQueueService = rescueQueueService;
        this.reassignmentService = reassignmentService;
    }

    @GetMapping("/rescue-queue")
    public ApiResponse<List<Map<String, Object>>> rescueQueue() {
        return ApiResponse.of(rescueQueueService.rescueQueue(), null);
    }

    @GetMapping("/{bookingId}/assignment-candidates")
    public ApiResponse<List<Map<String, Object>>> assignmentCandidates(@PathVariable UUID bookingId) {
        return ApiResponse.of(rescueQueueService.assignmentCandidates(bookingId), null);
    }

    @PostMapping("/{bookingId}/reassign")
    public ApiResponse<Map<String, Object>> reassign(
            @PathVariable UUID bookingId,
            @Valid @RequestBody ReassignRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(
                reassignmentService.adminReassign(
                        bookingId,
                        new AdminBookingService.ReassignCommand(
                                request.targetDriverId(),
                                request.reasonCode(),
                                request.reasonNote(),
                                request.rowVersion(),
                                request.overrideAcknowledged()
                        ),
                        idempotencyKey
                ),
                null
        );
    }

    public record ReassignRequest(
            @NotNull UUID targetDriverId,
            @NotBlank String reasonCode,
            String reasonNote,
            @NotNull Long rowVersion,
            boolean overrideAcknowledged
    ) {
    }
}

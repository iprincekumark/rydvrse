package com.rydvrse.support.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.support.application.SupportTicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class SupportController {

    private final SupportTicketService supportTicketService;

    public SupportController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping("/api/v1/support/tickets")
    public ApiResponse<Map<String, Object>> createCustomerTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(supportTicketService.createCustomerTicket(
                new SupportTicketService.CreateTicketCommand(
                        request.category(),
                        request.subCategory(),
                        request.severity(),
                        request.description(),
                        request.bookingId(),
                        request.tripId()
                ),
                idempotencyKey
        ), null);
    }

    @GetMapping("/api/v1/support/tickets")
    public ApiResponse<List<Map<String, Object>>> listCustomerTickets() {
        return ApiResponse.of(supportTicketService.listMine(), null);
    }

    @GetMapping("/api/v1/support/tickets/{ticketId}")
    public ApiResponse<Map<String, Object>> customerTicket(@PathVariable UUID ticketId) {
        return ApiResponse.of(supportTicketService.detail(ticketId), null);
    }

    @PostMapping("/api/v1/drivers/support/tickets")
    public ApiResponse<Map<String, Object>> createDriverTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(supportTicketService.createDriverTicket(
                new SupportTicketService.CreateTicketCommand(
                        request.category(),
                        request.subCategory(),
                        request.severity(),
                        request.description(),
                        request.bookingId(),
                        request.tripId()
                ),
                idempotencyKey
        ), null);
    }

    @GetMapping("/api/v1/admin/support/tickets")
    public ApiResponse<List<Map<String, Object>>> adminTickets() {
        return ApiResponse.of(supportTicketService.listAll(), null);
    }

    @GetMapping("/api/v1/admin/support/tickets/{ticketId}")
    public ApiResponse<Map<String, Object>> adminTicket(@PathVariable UUID ticketId) {
        return ApiResponse.of(supportTicketService.detail(ticketId), null);
    }

    @PostMapping("/api/v1/admin/support/tickets/{ticketId}/actions")
    public ApiResponse<Map<String, Object>> adminTicketAction(@PathVariable UUID ticketId, @RequestBody Map<String, Object> payload) {
        return ApiResponse.of(supportTicketService.adminAction(
                ticketId,
                String.valueOf(payload.get("action_type")),
                (Map<String, Object>) payload.getOrDefault("payload", Map.of())
        ), null);
    }

    @GetMapping("/api/v1/admin/incidents")
    public ApiResponse<List<Map<String, Object>>> incidents() {
        return ApiResponse.of(supportTicketService.incidents(), null);
    }

    @GetMapping("/api/v1/admin/incidents/{incidentId}")
    public ApiResponse<Map<String, Object>> incident(@PathVariable UUID incidentId) {
        return ApiResponse.of(supportTicketService.incident(incidentId), null);
    }

    @PostMapping("/api/v1/admin/incidents/{incidentId}/actions")
    public ApiResponse<Map<String, Object>> incidentAction(@PathVariable UUID incidentId, @RequestBody Map<String, Object> payload) {
        return ApiResponse.of(supportTicketService.incidentAction(
                incidentId,
                String.valueOf(payload.get("action_type")),
                (Map<String, Object>) payload.getOrDefault("payload", Map.of())
        ), null);
    }

    public record CreateTicketRequest(
            @NotBlank String category,
            String subCategory,
            @NotBlank String severity,
            @NotBlank String description,
            UUID bookingId,
            UUID tripId
    ) {
    }
}

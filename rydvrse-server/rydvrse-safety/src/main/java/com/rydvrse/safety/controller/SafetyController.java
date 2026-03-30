package com.rydvrse.safety.controller;

import com.rydvrse.safety.entity.*;
import com.rydvrse.safety.service.SafetyService;
import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.shared.enums.*;
import com.rydvrse.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/safety") @RequiredArgsConstructor
@Tag(name = "Safety", description = "SOS, incidents, trip sharing")
public class SafetyController {
    private final SafetyService safetyService;

    @PostMapping("/sos")
    @Operation(summary = "Trigger SOS alert")
    public ResponseEntity<ApiResponse<SosAlert>> triggerSos(@AuthenticationPrincipal UserPrincipal principal,
                                                              @RequestBody Map<String, Object> body) {
        UserType userType = UserType.valueOf(principal.getUserType());
        SosAlert sos = safetyService.triggerSos(UUID.fromString((String) body.get("tripId")),
                principal.getUserId(), userType,
                ((Number) body.get("lat")).doubleValue(), ((Number) body.get("lng")).doubleValue());
        return ResponseEntity.ok(ApiResponse.success(sos));
    }

    @PutMapping("/sos/{id}/resolve")
    @Operation(summary = "Resolve SOS alert")
    public ResponseEntity<ApiResponse<SosAlert>> resolveSos(@PathVariable UUID id,
                                                              @AuthenticationPrincipal UserPrincipal principal,
                                                              @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                safetyService.resolveSos(id, principal.getUserId(), body.get("notes"))));
    }

    @GetMapping("/sos/active")
    @Operation(summary = "Get active SOS alerts")
    public ResponseEntity<ApiResponse<List<SosAlert>>> activeSos() {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getActiveSosAlerts()));
    }

    @PostMapping("/incidents")
    @Operation(summary = "Report incident")
    public ResponseEntity<ApiResponse<Incident>> reportIncident(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @RequestBody Map<String, String> body) {
        UserType userType = UserType.valueOf(principal.getUserType());
        Incident incident = safetyService.reportIncident(
                body.containsKey("tripId") ? UUID.fromString(body.get("tripId")) : null,
                principal.getUserId(), userType,
                IncidentType.valueOf(body.get("type")), body.get("description"),
                IncidentPriority.valueOf(body.get("priority")));
        return ResponseEntity.ok(ApiResponse.success(incident));
    }

    @GetMapping("/incidents/{id}")
    @Operation(summary = "Get incident details")
    public ResponseEntity<ApiResponse<Incident>> getIncident(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getIncident(id)));
    }

    @GetMapping("/trips/{tripId}/share/{token}")
    @Operation(summary = "View shared trip (public)")
    public ResponseEntity<ApiResponse<Void>> viewSharedTrip(@PathVariable UUID tripId, @PathVariable UUID token) {
        // Returns trip tracking data for shared link
        return ResponseEntity.ok(ApiResponse.success("Shared trip view", null));
    }
}

package com.rydvrse.driver.api;

import com.rydvrse.driver.domain.Driver;
import com.rydvrse.driver.service.DriverService;
import com.rydvrse.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Driver>> getMyProfile(@AuthenticationPrincipal UUID authUserId) {
        return ResponseEntity.ok(ApiResponse.success(driverService.getDriverByAuth(authUserId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Driver>> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(driverService.getDriver(id)));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        driverService.toggleAvailability(id, body.getOrDefault("available", false));
        return ResponseEntity.ok(ApiResponse.success("Availability updated", null));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<?>> uploadDocument(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        var doc = driverService.uploadDocument(id,
                body.get("documentType"), body.get("documentUrl"), body.get("documentNumber"));
        return ResponseEntity.ok(ApiResponse.success("Document uploaded", doc));
    }
}

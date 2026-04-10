package com.rydvrse.customer.api;

import com.rydvrse.common.api.ApiMeta;
import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.common.util.RequestIdHolder;
import com.rydvrse.customer.application.SavedLocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/me/saved-locations")
public class SavedLocationController {

    private final SavedLocationService savedLocationService;

    public SavedLocationController(SavedLocationService savedLocationService) {
        this.savedLocationService = savedLocationService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return new ApiResponse<>(savedLocationService.list(), ApiMeta.now(RequestIdHolder.get(), Map.of("limit", 100, "next_cursor", null)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody SavedLocationRequest request) {
        return ApiResponse.of(savedLocationService.create(toCommand(request)), null);
    }

    @PatchMapping("/{savedLocationId}")
    public ApiResponse<Map<String, Object>> update(@PathVariable UUID savedLocationId, @Valid @RequestBody SavedLocationRequest request) {
        return ApiResponse.of(savedLocationService.update(savedLocationId, toCommand(request)), null);
    }

    @DeleteMapping("/{savedLocationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID savedLocationId) {
        savedLocationService.delete(savedLocationId);
    }

    private SavedLocationService.SavedLocationCommand toCommand(SavedLocationRequest request) {
        return new SavedLocationService.SavedLocationCommand(
                request.label(),
                request.location().addressLine1(),
                request.location().addressLine2(),
                request.location().landmark(),
                request.location().cityId(),
                request.location().latitude(),
                request.location().longitude(),
                request.isDefault()
        );
    }

    public record SavedLocationRequest(
            @NotBlank String label,
            @NotNull LocationRequest location,
            boolean isDefault
    ) {
    }

    public record LocationRequest(
            @NotBlank String addressLine1,
            String addressLine2,
            String landmark,
            @NotNull UUID cityId,
            @NotNull BigDecimal latitude,
            @NotNull BigDecimal longitude
    ) {
    }
}

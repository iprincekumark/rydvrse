package com.rydvrse.pricing.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.pricing.application.QuoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Object>> createQuote(
            @Valid @RequestBody CreateQuoteRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ApiResponse.of(quoteService.createQuote(new QuoteService.CreateQuoteCommand(
                request.serviceType(),
                toLocationInput(request.pickup()),
                request.drop() == null ? null : toLocationInput(request.drop()),
                request.scheduledPickupAt(),
                request.expectedDurationMinutes(),
                request.roundedDistanceKm(),
                request.predictedDriveMinutes(),
                request.driverPickupDistanceKm(),
                request.driverPickupEtaMinutes(),
                request.estimatedPickupCostPaise(),
                request.transmissionType(),
                request.carType(),
                request.carBrandModel(),
                request.carNumber(),
                request.roundTripWaitMinutes(),
                request.safetyAddonOpted(),
                request.customerNotes()
        )), null);
    }

    @GetMapping("/{quoteId}")
    public ApiResponse<Map<String, Object>> getQuote(@PathVariable UUID quoteId) {
        return ApiResponse.of(quoteService.getQuote(quoteId), null);
    }

    private QuoteService.LocationInput toLocationInput(LocationRequest request) {
        return new QuoteService.LocationInput(
                request.label(),
                request.addressLine1(),
                request.addressLine2(),
                request.landmark(),
                request.cityId(),
                request.latitude(),
                request.longitude()
        );
    }

    public record CreateQuoteRequest(
            @NotBlank String serviceType,
            @NotNull LocationRequest pickup,
            LocationRequest drop,
            @NotNull OffsetDateTime scheduledPickupAt,
            Integer expectedDurationMinutes,
            Integer roundedDistanceKm,
            Integer predictedDriveMinutes,
            Integer driverPickupDistanceKm,
            Integer driverPickupEtaMinutes,
            Integer estimatedPickupCostPaise,
            String transmissionType,
            String carType,
            String carBrandModel,
            String carNumber,
            Integer roundTripWaitMinutes,
            Boolean safetyAddonOpted,
            String customerNotes
    ) {
    }

    public record LocationRequest(
            String label,
            @NotBlank String addressLine1,
            String addressLine2,
            String landmark,
            @NotNull UUID cityId,
            @NotNull BigDecimal latitude,
            @NotNull BigDecimal longitude
    ) {
    }
}

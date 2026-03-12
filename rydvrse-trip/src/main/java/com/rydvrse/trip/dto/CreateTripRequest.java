package com.rydvrse.trip.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateTripRequest {
    @NotNull private UUID customerId;
    private UUID vehicleId;

    @NotNull private Double pickupLat;
    @NotNull private Double pickupLng;
    private String pickupAddress;
    private String pickupCity;

    @NotNull private Double dropLat;
    @NotNull private Double dropLng;
    private String dropAddress;
    private String dropCity;

    private Double estimatedFare;
    private Double surgeMultiplier;
}

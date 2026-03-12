package com.rydvrse.pricing.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FareEstimate {
    private double baseFare;
    private double distanceCharge;
    private double timeCharge;
    private double surgeMultiplier;
    private boolean nightSurcharge;
    private double estimatedFare;
    private String currency;
}

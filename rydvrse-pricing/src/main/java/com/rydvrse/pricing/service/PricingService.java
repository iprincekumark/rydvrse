package com.rydvrse.pricing.service;

import com.rydvrse.pricing.domain.PricingRule;
import com.rydvrse.pricing.dto.FareEstimate;
import com.rydvrse.pricing.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

/**
 * Pricing engine — calculates fare estimates and final fares.
 *
 * Fare formula:
 * fare = baseFare + (distance * perKmRate) + (duration * perMinuteRate)
 * fare = fare * surgeMultiplier
 * fare = max(fare, minimumFare)
 * if nightTime: fare = fare * (1 + nightSurchargePct/100)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRuleRepository pricingRuleRepository;

    @Value("${rydvrse.pricing.base-fare:50.0}")
    private double defaultBaseFare;

    @Value("${rydvrse.pricing.per-km-rate:12.0}")
    private double defaultPerKmRate;

    @Value("${rydvrse.pricing.per-minute-rate:2.0}")
    private double defaultPerMinuteRate;

    @Value("${rydvrse.pricing.minimum-fare:80.0}")
    private double defaultMinimumFare;

    @Value("${rydvrse.pricing.surge-multiplier-cap:3.0}")
    private double surgeMultiplierCap;

    /**
     * Calculate fare estimate for a trip.
     */
    public FareEstimate calculateEstimate(String city, String vehicleType,
                                           double distanceKm, int estimatedMinutes,
                                           double surgeMultiplier) {
        PricingRule rule = pricingRuleRepository
                .findByCityAndVehicleTypeAndIsActiveTrue(city, vehicleType)
                .orElse(getDefaultRule());

        double surge = Math.min(surgeMultiplier, surgeMultiplierCap);
        double baseFare = rule.getBaseFare();
        double distanceCharge = distanceKm * rule.getPerKmRate();
        double timeCharge = estimatedMinutes * rule.getPerMinuteRate();
        double subtotal = baseFare + distanceCharge + timeCharge;
        double surgedFare = subtotal * surge;

        // Night surcharge (11 PM to 6 AM)
        boolean isNight = isNightTime();
        if (isNight) {
            surgedFare *= (1 + rule.getNightSurchargePct() / 100);
        }

        double finalFare = Math.max(surgedFare, rule.getMinimumFare());
        finalFare = Math.round(finalFare * 100.0) / 100.0;

        return FareEstimate.builder()
                .baseFare(baseFare)
                .distanceCharge(Math.round(distanceCharge * 100.0) / 100.0)
                .timeCharge(Math.round(timeCharge * 100.0) / 100.0)
                .surgeMultiplier(surge)
                .nightSurcharge(isNight)
                .estimatedFare(finalFare)
                .currency("INR")
                .build();
    }

    private PricingRule getDefaultRule() {
        return PricingRule.builder()
                .baseFare(defaultBaseFare)
                .perKmRate(defaultPerKmRate)
                .perMinuteRate(defaultPerMinuteRate)
                .minimumFare(defaultMinimumFare)
                .nightSurchargePct(25.0)
                .build();
    }

    private boolean isNightTime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(23, 0)) || now.isBefore(LocalTime.of(6, 0));
    }
}

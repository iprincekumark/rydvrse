package com.rydvrse.pricing.application;

import org.springframework.stereotype.Component;

@Component
public class DriverAcquisitionEstimator {

    static final int PICKUP_ACCESS_FLOOR_PAISE = 2_900;
    static final int PICKUP_ACCESS_CAP_PAISE = 4_900;

    public AcquisitionEstimate estimate(Integer driverPickupDistanceKm, Integer driverPickupEtaMinutes, Integer estimatedPickupCostPaise) {
        int rawCost = estimatedPickupCostPaise == null || estimatedPickupCostPaise <= 0
                ? estimateRawCost(driverPickupDistanceKm, driverPickupEtaMinutes)
                : estimatedPickupCostPaise;
        int cappedCost = Math.min(Math.max(roundToNearestFiveRupees(rawCost), PICKUP_ACCESS_FLOOR_PAISE), PICKUP_ACCESS_CAP_PAISE);
        return new AcquisitionEstimate(rawCost, cappedCost, driverPickupDistanceKm, driverPickupEtaMinutes, 30);
    }

    private int estimateRawCost(Integer driverPickupDistanceKm, Integer driverPickupEtaMinutes) {
        int distance = driverPickupDistanceKm == null || driverPickupDistanceKm <= 0 ? 6 : driverPickupDistanceKm;
        int eta = driverPickupEtaMinutes == null || driverPickupEtaMinutes <= 0 ? 20 : driverPickupEtaMinutes;
        int distanceCost = distance * 500;
        int timeCost = Math.max(eta - 15, 0) * 100;
        return distanceCost + timeCost;
    }

    private int roundToNearestFiveRupees(int amountPaise) {
        return Math.round(amountPaise / 500.0f) * 500;
    }

    public record AcquisitionEstimate(
            int rawCostPaise,
            int customerFeePaise,
            Integer driverPickupDistanceKm,
            Integer driverPickupEtaMinutes,
            int arrivalSlaMinutes
    ) {
    }
}

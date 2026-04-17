package com.rydvrse.pricing.application;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DriverPayoutPreviewCalculator {

    public PayoutPreview oneWay(DriverPayoutInput input) {
        List<PayoutComponent> components = new ArrayList<>();
        components.add(new PayoutComponent("DRIVER_BASE", "Driver base payout", 21_000));

        int extraDistanceKm = Math.max(input.distanceKm() - 20, 0);
        if (extraDistanceKm > 0) {
            components.add(new PayoutComponent("DISTANCE_PAYOUT", "Distance payout", extraDistanceKm * 400L));
        }

        int includedMinutes = input.includedMinutes();
        int extraMinutes = Math.max(input.predictedDriveMinutes() - includedMinutes, 0);
        if (extraMinutes > 0) {
            components.add(new PayoutComponent("TIME_PAYOUT", "Traffic-time payout", extraMinutes * 120L));
        }

        components.add(new PayoutComponent("PICKUP_ACCESS_PAYOUT", "Pickup access pass-through", input.pickupAccessFeePaise()));
        components.add(new PayoutComponent("RELOCATION_PAYOUT", "One-way relocation pass-through", 5_900));
        addVehicleBonus(components, input);
        addPeakAndNightBonus(components, input);
        return new PayoutPreview(total(components), components);
    }

    public PayoutPreview roundTrip(DriverPayoutInput input) {
        List<PayoutComponent> components = new ArrayList<>();
        components.add(new PayoutComponent("ROUND_TRIP_BASE_PAYOUT", "Round-trip base payout", 56_000));

        int extraDistanceKm = Math.max(input.distanceKm() - 50, 0);
        if (extraDistanceKm > 0) {
            components.add(new PayoutComponent("ROUND_TRIP_DISTANCE_PAYOUT", "Round-trip distance payout", extraDistanceKm * 375L));
        }

        int extraMinutes = Math.max(input.predictedDriveMinutes() - 240, 0);
        if (extraMinutes > 0) {
            components.add(new PayoutComponent("ROUND_TRIP_TIME_PAYOUT", "Round-trip time payout", extraMinutes * 100L));
        }

        components.add(new PayoutComponent("PICKUP_ACCESS_PAYOUT", "Pickup access pass-through", input.pickupAccessFeePaise()));
        addVehicleBonus(components, input);
        addPeakAndNightBonus(components, input);
        return new PayoutPreview(total(components), components);
    }

    private void addVehicleBonus(List<PayoutComponent> components, DriverPayoutInput input) {
        String carType = input.carType() == null ? "" : input.carType().toUpperCase();
        String transmission = input.transmissionType() == null ? "" : input.transmissionType().toUpperCase();
        if ("SUV".equals(carType)) {
            components.add(new PayoutComponent("VEHICLE_BONUS", "SUV handling bonus", 2_000));
        } else if ("LUXURY".equals(carType)) {
            components.add(new PayoutComponent("VEHICLE_BONUS", "Luxury vehicle handling bonus", 5_000));
        }
        if ("AUTOMATIC".equals(transmission)) {
            components.add(new PayoutComponent("TRANSMISSION_BONUS", "Automatic transmission bonus", 2_000));
        }
    }

    private void addPeakAndNightBonus(List<PayoutComponent> components, DriverPayoutInput input) {
        if (input.peakTrafficFeePaise() > 0) {
            components.add(new PayoutComponent("PEAK_BONUS", "Peak traffic bonus", Math.round(input.peakTrafficFeePaise() * 0.70)));
        }
        if (input.nightFeePaise() > 0) {
            components.add(new PayoutComponent("NIGHT_BONUS", "Night bonus", 6_000));
        }
    }

    private long total(List<PayoutComponent> components) {
        return components.stream().mapToLong(PayoutComponent::amountPaise).sum();
    }

    public Map<String, Object> toMap(PayoutPreview preview) {
        return Map.of(
                "total_payout_paise", preview.totalPayoutPaise(),
                "components", preview.components().stream()
                        .map(component -> Map.of(
                                "code", component.code(),
                                "label", component.label(),
                                "amount_paise", component.amountPaise()
                        ))
                        .toList()
        );
    }

    public record DriverPayoutInput(
            int distanceKm,
            int predictedDriveMinutes,
            int includedMinutes,
            long pickupAccessFeePaise,
            String transmissionType,
            String carType,
            long peakTrafficFeePaise,
            long nightFeePaise
    ) {
    }

    public record PayoutPreview(long totalPayoutPaise, List<PayoutComponent> components) {
    }

    public record PayoutComponent(String code, String label, long amountPaise) {
    }
}

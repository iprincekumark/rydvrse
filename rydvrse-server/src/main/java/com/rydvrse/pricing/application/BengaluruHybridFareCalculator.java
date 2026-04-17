package com.rydvrse.pricing.application;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BengaluruHybridFareCalculator {

    private static final long ONE_WAY_BASE_PAISE = 29_900;
    private static final int ONE_WAY_INCLUDED_DISTANCE_KM = 20;
    private static final int ONE_WAY_INCLUDED_MINUTES = 75;
    private static final long ONE_WAY_20_TO_35_RATE_PAISE = 650;
    private static final long ONE_WAY_35_PLUS_RATE_PAISE = 800;
    private static final long ONE_WAY_EXTRA_MINUTE_RATE_PAISE = 175;
    private static final long ONE_WAY_RELOCATION_PAISE = 5_900;

    private static final long ROUND_TRIP_BASE_PAISE = 64_900;
    private static final int ROUND_TRIP_INCLUDED_DISTANCE_KM = 50;
    private static final int ROUND_TRIP_INCLUDED_MINUTES = 240;
    private static final long ROUND_TRIP_EXTRA_KM_RATE_PAISE = 600;
    private static final long ROUND_TRIP_EXTRA_MINUTE_RATE_PAISE = 150;

    private static final long SAFETY_FEE_PAISE = 1_200;
    private static final long PEAK_TRAFFIC_FEE_PAISE = 3_900;
    private static final long NIGHT_FEE_PAISE = 9_900;
    private static final int REFERENCE_ONE_WAY_31KM_PAISE = 66_400;

    private final DriverAcquisitionEstimator acquisitionEstimator;
    private final DriverPayoutPreviewCalculator payoutPreviewCalculator;

    public BengaluruHybridFareCalculator(
            DriverAcquisitionEstimator acquisitionEstimator,
            DriverPayoutPreviewCalculator payoutPreviewCalculator
    ) {
        this.acquisitionEstimator = acquisitionEstimator;
        this.payoutPreviewCalculator = payoutPreviewCalculator;
    }

    public HybridQuote calculate(HybridQuoteRequest request) {
        if (isRoundTrip(request.serviceType())) {
            return roundTrip(request);
        }
        return oneWay(request);
    }

    public boolean supports(String serviceType) {
        return isOneWay(serviceType) || isRoundTrip(serviceType);
    }

    private HybridQuote oneWay(HybridQuoteRequest request) {
        int distanceKm = positiveOrDefault(request.roundedDistanceKm(), 31);
        int predictedMinutes = positiveOrDefault(request.predictedDriveMinutes(), request.expectedDurationMinutes() == null ? 105 : request.expectedDurationMinutes());
        int includedMinutes = ONE_WAY_INCLUDED_MINUTES + (int) Math.round(Math.max(distanceKm - ONE_WAY_INCLUDED_DISTANCE_KM, 0) * 2.2);
        DriverAcquisitionEstimator.AcquisitionEstimate acquisition = acquisitionEstimator.estimate(
                request.driverPickupDistanceKm(),
                request.driverPickupEtaMinutes(),
                request.estimatedPickupCostPaise()
        );

        List<HybridLineItem> items = new ArrayList<>();
        items.add(new HybridLineItem("BLR_ONE_WAY_BASE", "Base fare: first 20 km + 75 min", ONE_WAY_BASE_PAISE, Map.of(
                "included_distance_km", ONE_WAY_INCLUDED_DISTANCE_KM,
                "included_minutes", ONE_WAY_INCLUDED_MINUTES
        )));

        int midDistanceKm = Math.min(Math.max(distanceKm - ONE_WAY_INCLUDED_DISTANCE_KM, 0), 15);
        if (midDistanceKm > 0) {
            items.add(new HybridLineItem("BLR_DISTANCE_20_35", "Distance fee: " + midDistanceKm + " km x Rs. 6.50", midDistanceKm * ONE_WAY_20_TO_35_RATE_PAISE, Map.of(
                    "distance_km", midDistanceKm,
                    "rate_paise_per_km", ONE_WAY_20_TO_35_RATE_PAISE
            )));
        }

        int longDistanceKm = Math.max(distanceKm - 35, 0);
        if (longDistanceKm > 0) {
            items.add(new HybridLineItem("BLR_DISTANCE_35_PLUS", "Long-distance fee: " + longDistanceKm + " km x Rs. 8", longDistanceKm * ONE_WAY_35_PLUS_RATE_PAISE, Map.of(
                    "distance_km", longDistanceKm,
                    "rate_paise_per_km", ONE_WAY_35_PLUS_RATE_PAISE
            )));
        }

        int extraTrafficMinutes = Math.max(predictedMinutes - includedMinutes, 0);
        if (extraTrafficMinutes > 0) {
            items.add(new HybridLineItem("BLR_TRAFFIC_TIME", "Traffic time buffer", roundToNearestFiveRupees(extraTrafficMinutes * ONE_WAY_EXTRA_MINUTE_RATE_PAISE), Map.of(
                    "extra_minutes", extraTrafficMinutes,
                    "rate_paise_per_minute", ONE_WAY_EXTRA_MINUTE_RATE_PAISE
            )));
        }

        items.add(new HybridLineItem("BLR_PICKUP_ACCESS", "Driver pickup access", acquisition.customerFeePaise(), Map.of(
                "raw_cost_paise", acquisition.rawCostPaise(),
                "arrival_sla_minutes", acquisition.arrivalSlaMinutes()
        )));
        items.add(new HybridLineItem("BLR_ONE_WAY_RELOCATION", "One-way relocation allowance", ONE_WAY_RELOCATION_PAISE, Map.of()));
        addVehicleAdjustment(items, request);
        long peakFee = addPeakFee(items, request.peakWindow());
        long nightFee = addNightFee(items, request.nightWindow());
        addSafetyFee(items, request.safetyAddonOpted());

        DriverPayoutPreviewCalculator.PayoutPreview payoutPreview = payoutPreviewCalculator.oneWay(new DriverPayoutPreviewCalculator.DriverPayoutInput(
                distanceKm,
                predictedMinutes,
                includedMinutes,
                acquisition.customerFeePaise(),
                request.transmissionType(),
                request.carType(),
                peakFee,
                nightFee
        ));

        Map<String, Object> savingsSummary = new LinkedHashMap<>();
        long customerSubtotal = items.stream().mapToLong(HybridLineItem::amountPaise).sum();
        if (distanceKm == 31) {
            savingsSummary.put("reference_total_paise", REFERENCE_ONE_WAY_31KM_PAISE);
            savingsSummary.put("estimated_savings_paise", Math.max(REFERENCE_ONE_WAY_31KM_PAISE - customerSubtotal, 0));
            savingsSummary.put("message", "Estimated lower than the reference one-way model before tax and night charges.");
        }

        return new HybridQuote(
                "BLR_HYBRID_ONE_WAY_V1",
                items,
                assumptions(request, distanceKm, predictedMinutes, ONE_WAY_INCLUDED_DISTANCE_KM, includedMinutes, acquisition, "CLIENT_OR_MAP_ESTIMATE"),
                payoutPreviewCalculator.toMap(payoutPreview),
                savingsSummary
        );
    }

    private HybridQuote roundTrip(HybridQuoteRequest request) {
        int distanceKm = positiveOrDefault(request.roundedDistanceKm(), 62);
        int predictedMinutes = positiveOrDefault(request.predictedDriveMinutes(), request.expectedDurationMinutes() == null ? 300 : request.expectedDurationMinutes());
        DriverAcquisitionEstimator.AcquisitionEstimate acquisition = acquisitionEstimator.estimate(
                request.driverPickupDistanceKm(),
                request.driverPickupEtaMinutes(),
                request.estimatedPickupCostPaise()
        );

        List<HybridLineItem> items = new ArrayList<>();
        items.add(new HybridLineItem("BLR_ROUND_TRIP_BUNDLE_BASE", "Round-trip bundle: first 50 km + 240 min", ROUND_TRIP_BASE_PAISE, Map.of(
                "included_distance_km", ROUND_TRIP_INCLUDED_DISTANCE_KM,
                "included_minutes", ROUND_TRIP_INCLUDED_MINUTES
        )));

        int extraDistanceKm = Math.max(distanceKm - ROUND_TRIP_INCLUDED_DISTANCE_KM, 0);
        if (extraDistanceKm > 0) {
            items.add(new HybridLineItem("BLR_ROUND_TRIP_DISTANCE", "Bundle extra distance", extraDistanceKm * ROUND_TRIP_EXTRA_KM_RATE_PAISE, Map.of(
                    "distance_km", extraDistanceKm,
                    "rate_paise_per_km", ROUND_TRIP_EXTRA_KM_RATE_PAISE
            )));
        }

        int extraMinutes = Math.max(predictedMinutes - ROUND_TRIP_INCLUDED_MINUTES, 0);
        if (extraMinutes > 0) {
            items.add(new HybridLineItem("BLR_ROUND_TRIP_TIME", "Bundle extra traffic time", extraMinutes * ROUND_TRIP_EXTRA_MINUTE_RATE_PAISE, Map.of(
                    "extra_minutes", extraMinutes,
                    "rate_paise_per_minute", ROUND_TRIP_EXTRA_MINUTE_RATE_PAISE
            )));
        }

        items.add(new HybridLineItem("BLR_PICKUP_ACCESS", "Driver pickup access", acquisition.customerFeePaise(), Map.of(
                "raw_cost_paise", acquisition.rawCostPaise(),
                "arrival_sla_minutes", acquisition.arrivalSlaMinutes()
        )));
        addVehicleAdjustment(items, request);
        long peakFee = addPeakFee(items, request.peakWindow());
        long nightFee = addNightFee(items, request.nightWindow());
        addSafetyFee(items, request.safetyAddonOpted());

        DriverPayoutPreviewCalculator.PayoutPreview payoutPreview = payoutPreviewCalculator.roundTrip(new DriverPayoutPreviewCalculator.DriverPayoutInput(
                distanceKm,
                predictedMinutes,
                ROUND_TRIP_INCLUDED_MINUTES,
                acquisition.customerFeePaise(),
                request.transmissionType(),
                request.carType(),
                peakFee,
                nightFee
        ));

        Map<String, Object> savingsSummary = new LinkedHashMap<>();
        savingsSummary.put("message", "Round trip is priced as a single-driver bundle, so pickup access is charged once instead of twice.");
        savingsSummary.put("bundle_discount_basis", "lower extra-km and extra-minute rates versus two one-way bookings");

        return new HybridQuote(
                "BLR_HYBRID_ROUND_TRIP_V1",
                items,
                assumptions(request, distanceKm, predictedMinutes, ROUND_TRIP_INCLUDED_DISTANCE_KM, ROUND_TRIP_INCLUDED_MINUTES, acquisition, "CLIENT_OR_MAP_ESTIMATE"),
                payoutPreviewCalculator.toMap(payoutPreview),
                savingsSummary
        );
    }

    private void addVehicleAdjustment(List<HybridLineItem> items, HybridQuoteRequest request) {
        String carType = request.carType() == null ? "" : request.carType().toUpperCase();
        String transmission = request.transmissionType() == null ? "" : request.transmissionType().toUpperCase();
        long adjustment = 0;
        String label = "Vehicle handling adjustment";
        if ("SUV".equals(carType)) {
            adjustment += 2_500;
            label = "SUV handling adjustment";
        } else if ("LUXURY".equals(carType)) {
            adjustment += 6_000;
            label = "Luxury vehicle handling adjustment";
        }
        if ("AUTOMATIC".equals(transmission)) {
            adjustment += 1_500;
        }
        if (adjustment > 0) {
            items.add(new HybridLineItem("BLR_VEHICLE_ADJUSTMENT", label, adjustment, Map.of(
                    "car_type", nullSafe(request.carType()),
                    "transmission_type", nullSafe(request.transmissionType())
            )));
        }
    }

    private long addPeakFee(List<HybridLineItem> items, boolean peakWindow) {
        if (!peakWindow) {
            return 0;
        }
        items.add(new HybridLineItem("BLR_PEAK_TRAFFIC_FEE", "Capped peak traffic risk fee", PEAK_TRAFFIC_FEE_PAISE, Map.of(
                "cap_paise", PEAK_TRAFFIC_FEE_PAISE
        )));
        return PEAK_TRAFFIC_FEE_PAISE;
    }

    private long addNightFee(List<HybridLineItem> items, boolean nightWindow) {
        if (!nightWindow) {
            return 0;
        }
        items.add(new HybridLineItem("NIGHT_SURCHARGE", "Night driver protection fee", NIGHT_FEE_PAISE, Map.of()));
        return NIGHT_FEE_PAISE;
    }

    private void addSafetyFee(List<HybridLineItem> items, boolean safetyAddonOpted) {
        if (safetyAddonOpted) {
            items.add(new HybridLineItem("RYD_SECURE", "Rydvrse Secure", SAFETY_FEE_PAISE, Map.of(
                    "coverage_note", "optional safety support add-on"
            )));
        }
    }

    private Map<String, Object> assumptions(
            HybridQuoteRequest request,
            int distanceKm,
            int predictedMinutes,
            int includedDistanceKm,
            int includedMinutes,
            DriverAcquisitionEstimator.AcquisitionEstimate acquisition,
            String estimateQuality
    ) {
        Map<String, Object> assumptions = new LinkedHashMap<>();
        assumptions.put("rounded_distance_km", distanceKm);
        assumptions.put("predicted_drive_minutes", predictedMinutes);
        assumptions.put("included_distance_km", includedDistanceKm);
        assumptions.put("included_minutes", includedMinutes);
        assumptions.put("driver_pickup_distance_km", acquisition.driverPickupDistanceKm());
        assumptions.put("driver_pickup_eta_minutes", acquisition.driverPickupEtaMinutes());
        assumptions.put("estimated_pickup_cost_paise", acquisition.rawCostPaise());
        assumptions.put("pickup_arrival_sla_minutes", acquisition.arrivalSlaMinutes());
        assumptions.put("transmission_type", nullSafe(request.transmissionType()));
        assumptions.put("car_type", nullSafe(request.carType()));
        assumptions.put("car_brand_model", nullSafe(request.carBrandModel()));
        assumptions.put("car_number_masked", maskCarNumber(request.carNumber()));
        assumptions.put("safety_addon_opted", request.safetyAddonOpted());
        assumptions.put("estimate_quality", estimateQuality);
        return assumptions;
    }

    private int positiveOrDefault(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private long roundToNearestFiveRupees(long amountPaise) {
        return Math.round(amountPaise / 500.0f) * 500L;
    }

    private boolean isOneWay(String serviceType) {
        return "SCHEDULED_ONE_WAY".equals(serviceType) || "ONE_WAY_DROP".equals(serviceType);
    }

    private boolean isRoundTrip(String serviceType) {
        return "SCHEDULED_ROUND_TRIP".equals(serviceType) || "ROUND_TRIP".equals(serviceType);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String maskCarNumber(String carNumber) {
        if (carNumber == null || carNumber.length() < 4) {
            return "";
        }
        return "****" + carNumber.substring(carNumber.length() - 4);
    }

    public record HybridQuoteRequest(
            String serviceType,
            Integer roundedDistanceKm,
            Integer predictedDriveMinutes,
            Integer expectedDurationMinutes,
            Integer driverPickupDistanceKm,
            Integer driverPickupEtaMinutes,
            Integer estimatedPickupCostPaise,
            String transmissionType,
            String carType,
            String carBrandModel,
            String carNumber,
            boolean safetyAddonOpted,
            boolean peakWindow,
            boolean nightWindow
    ) {
    }

    public record HybridQuote(
            String commercialModel,
            List<HybridLineItem> lineItems,
            Map<String, Object> pricingAssumptions,
            Map<String, Object> driverPayoutPreview,
            Map<String, Object> savingsSummary
    ) {
    }

    public record HybridLineItem(String code, String label, long amountPaise, Map<String, Object> metadata) {
    }
}

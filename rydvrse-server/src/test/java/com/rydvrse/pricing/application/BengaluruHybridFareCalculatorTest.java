package com.rydvrse.pricing.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BengaluruHybridFareCalculatorTest {

    private final DriverPayoutPreviewCalculator payoutPreviewCalculator = new DriverPayoutPreviewCalculator();
    private final BengaluruHybridFareCalculator calculator = new BengaluruHybridFareCalculator(
            new DriverAcquisitionEstimator(),
            payoutPreviewCalculator
    );

    @Test
    void oneWayThirtyOneKmShouldBeatReferenceSubtotalBeforeTax() {
        BengaluruHybridFareCalculator.HybridQuote quote = calculator.calculate(new BengaluruHybridFareCalculator.HybridQuoteRequest(
                "SCHEDULED_ONE_WAY",
                31,
                105,
                105,
                8,
                24,
                null,
                "MANUAL",
                "SEDAN",
                "Hyundai Verna",
                "KA03AB1234",
                true,
                false,
                false
        ));

        long subtotal = quote.lineItems().stream().mapToLong(BengaluruHybridFareCalculator.HybridLineItem::amountPaise).sum();

        assertThat(quote.commercialModel()).isEqualTo("BLR_HYBRID_ONE_WAY_V1");
        assertThat(subtotal).isLessThan(66_400);
        assertThat(subtotal).isEqualTo(50_050);
        assertThat(quote.driverPayoutPreview()).containsEntry("total_payout_paise", 36_920L);
        assertThat(quote.pricingAssumptions()).containsEntry("rounded_distance_km", 31);
    }

    @Test
    void roundTripShouldChargePickupAccessOnlyOnce() {
        BengaluruHybridFareCalculator.HybridQuote quote = calculator.calculate(new BengaluruHybridFareCalculator.HybridQuoteRequest(
                "SCHEDULED_ROUND_TRIP",
                62,
                300,
                300,
                8,
                24,
                null,
                "AUTOMATIC",
                "SEDAN",
                "Hyundai Verna",
                "KA03AB1234",
                true,
                false,
                false
        ));

        long pickupAccessComponents = quote.lineItems().stream()
                .filter(component -> "BLR_PICKUP_ACCESS".equals(component.code()))
                .count();

        assertThat(quote.commercialModel()).isEqualTo("BLR_HYBRID_ROUND_TRIP_V1");
        assertThat(pickupAccessComponents).isEqualTo(1);
        assertThat(quote.savingsSummary().get("message")).asString().contains("single-driver bundle");
    }
}

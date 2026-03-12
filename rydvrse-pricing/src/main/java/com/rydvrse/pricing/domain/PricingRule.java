package com.rydvrse.pricing.domain;

import com.rydvrse.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * City-specific pricing rules.
 * Each city can have different base fares, per-km rates, and surge boundaries.
 */
@Entity
@Table(name = "pricing_rules", indexes = {
        @Index(name = "idx_pricing_city", columnList = "city")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingRule extends BaseEntity {

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "vehicle_type", length = 30)
    private String vehicleType; // SEDAN, SUV, HATCHBACK, ANY

    @Column(name = "base_fare", nullable = false)
    @Builder.Default
    private Double baseFare = 50.0;

    @Column(name = "per_km_rate", nullable = false)
    @Builder.Default
    private Double perKmRate = 12.0;

    @Column(name = "per_minute_rate", nullable = false)
    @Builder.Default
    private Double perMinuteRate = 2.0;

    @Column(name = "minimum_fare", nullable = false)
    @Builder.Default
    private Double minimumFare = 80.0;

    @Column(name = "night_surcharge_pct")
    @Builder.Default
    private Double nightSurchargePct = 25.0; // 25% extra 11PM-6AM

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}

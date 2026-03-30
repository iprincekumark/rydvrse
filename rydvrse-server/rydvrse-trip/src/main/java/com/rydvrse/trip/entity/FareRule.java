package com.rydvrse.trip.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fare_rule")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FareRule {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "vehicle_type", nullable = false, length = 50) private String vehicleType;
    @Column(name = "base_fare", nullable = false, precision = 10, scale = 2) private BigDecimal baseFare;
    @Column(name = "per_km_rate", nullable = false, precision = 10, scale = 2) private BigDecimal perKmRate;
    @Column(name = "per_minute_rate", nullable = false, precision = 10, scale = 2) private BigDecimal perMinuteRate;
    @Column(name = "minimum_fare", nullable = false, precision = 10, scale = 2) private BigDecimal minimumFare;
    @Column(name = "night_surcharge_percent", nullable = false, precision = 5, scale = 2) @Builder.Default private BigDecimal nightSurchargePercent = BigDecimal.ZERO;
    @Column(name = "cancellation_fee", nullable = false, precision = 10, scale = 2) @Builder.Default private BigDecimal cancellationFee = BigDecimal.ZERO;
    @Column(name = "effective_from", nullable = false) private LocalDate effectiveFrom;
    @Column(name = "effective_to") private LocalDate effectiveTo;
}

package com.rydvrse.vehicle.domain;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Vehicle entity — represents a customer's personal vehicle.
 * In RYDVRSE, drivers operate the CUSTOMER'S vehicle, so vehicles
 * are optionally linked to customers (for frequent bookings).
 */
@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_reg", columnList = "registration_number", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle extends BaseEntity {

    @Column(name = "owner_id")
    private UUID ownerId; // Customer who owns this vehicle

    @Column(name = "registration_number", nullable = false, unique = true, length = 20)
    private String registrationNumber;

    @Column(name = "make", length = 50)
    private String make; // e.g., Maruti, Hyundai, Tata

    @Column(name = "model", length = 50)
    private String model;

    @Column(name = "year")
    private Integer year;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "fuel_type", length = 20)
    private String fuelType; // PETROL, DIESEL, CNG, ELECTRIC

    @Column(name = "transmission", length = 20)
    private String transmission; // MANUAL, AUTOMATIC

    @Column(name = "vehicle_type", length = 30)
    private String vehicleType; // SEDAN, SUV, HATCHBACK

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VehicleDocument> documents = new ArrayList<>();
}

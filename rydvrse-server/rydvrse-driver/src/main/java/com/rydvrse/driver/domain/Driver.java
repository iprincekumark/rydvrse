package com.rydvrse.driver.domain;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.domain.GeoLocation;
import com.rydvrse.shared.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Driver entity — professional driver profile.
 * Owns onboarding state, verification status, availability, and performance metrics.
 */
@Entity
@Table(name = "drivers", indexes = {
        @Index(name = "idx_driver_auth", columnList = "auth_user_id", unique = true),
        @Index(name = "idx_driver_phone", columnList = "phone_number", unique = true),
        @Index(name = "idx_driver_status", columnList = "status"),
        @Index(name = "idx_driver_available", columnList = "is_available")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Driver extends BaseEntity {

    @Column(name = "auth_user_id", nullable = false, unique = true)
    private UUID authUserId;

    @Column(name = "phone_number", nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "license_expiry")
    private LocalDate licenseExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DriverStatus status = DriverStatus.PENDING_VERIFICATION;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = false;

    @Column(name = "is_on_trip", nullable = false)
    @Builder.Default
    private Boolean isOnTrip = false;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "current_lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "current_lng")),
            @AttributeOverride(name = "address", column = @Column(name = "current_address")),
            @AttributeOverride(name = "city", column = @Column(name = "current_city")),
            @AttributeOverride(name = "pincode", column = @Column(name = "current_pincode"))
    })
    private GeoLocation currentLocation;

    // Performance metrics
    @Column(name = "total_trips")
    @Builder.Default
    private Integer totalTrips = 0;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 5.0;

    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "acceptance_rate")
    @Builder.Default
    private Double acceptanceRate = 100.0;

    @Column(name = "cancellation_rate")
    @Builder.Default
    private Double cancellationRate = 0.0;

    @Column(name = "total_earnings")
    @Builder.Default
    private Double totalEarnings = 0.0;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DriverDocument> documents = new ArrayList<>();

    /** City where the driver primarily operates */
    @Column(name = "operating_city", length = 100)
    private String operatingCity;
}

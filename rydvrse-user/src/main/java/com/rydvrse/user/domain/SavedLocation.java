package com.rydvrse.user.domain;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.domain.GeoLocation;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saved_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SavedLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "label", nullable = false, length = 50)
    private String label; // e.g., "Home", "Office", "Gym"

    @Embedded
    private GeoLocation location;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}

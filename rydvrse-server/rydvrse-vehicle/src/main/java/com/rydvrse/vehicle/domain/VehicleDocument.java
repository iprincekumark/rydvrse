package com.rydvrse.vehicle.domain;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // RC, INSURANCE, PUC, FITNESS

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
}

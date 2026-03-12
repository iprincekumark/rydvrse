package com.rydvrse.driver.domain;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "driver_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // DRIVING_LICENSE, AADHAAR, PAN, PHOTO, ADDRESS_PROOF

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "verified_by")
    private String verifiedBy;
}

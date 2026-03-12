package com.rydvrse.support.domain;

import com.rydvrse.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "support_tickets", indexes = {
        @Index(name = "idx_ticket_user", columnList = "user_id"),
        @Index(name = "idx_ticket_status", columnList = "status"),
        @Index(name = "idx_ticket_number", columnList = "ticket_number", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportTicket extends BaseEntity {

    @Column(name = "ticket_number", unique = true, nullable = false, length = 20)
    private String ticketNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType; // CUSTOMER, DRIVER

    @Column(name = "trip_id")
    private UUID tripId;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // PAYMENT, TRIP, SAFETY, DRIVER, OTHER

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @Column(name = "priority", length = 10)
    @Builder.Default
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @PrePersist
    public void generateTicketNumber() {
        if (this.ticketNumber == null) {
            this.ticketNumber = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}

package com.rydvrse.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "customer_address")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "label", nullable = false, length = 50)
    private String label;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;
}

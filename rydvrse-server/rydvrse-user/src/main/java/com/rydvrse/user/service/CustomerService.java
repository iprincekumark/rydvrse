package com.rydvrse.user.service;

import com.rydvrse.auth.event.UserRegisteredEvent;
import com.rydvrse.shared.enums.UserRole;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import com.rydvrse.user.domain.Customer;
import com.rydvrse.user.dto.CustomerProfileRequest;
import com.rydvrse.user.dto.CustomerResponse;
import com.rydvrse.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Customer lifecycle service.
 * Listens to UserRegisteredEvent from Auth module to auto-create profiles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Event listener: auto-create customer profile on registration.
     */
    @EventListener
    @Transactional
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.getRole() != UserRole.CUSTOMER) return;

        if (customerRepository.existsByPhoneNumber(event.getPhoneNumber())) {
            log.warn("Customer profile already exists for phone: {}", event.getPhoneNumber());
            return;
        }

        Customer customer = Customer.builder()
                .authUserId(event.getAggregateId())
                .phoneNumber(event.getPhoneNumber())
                .build();
        customerRepository.save(customer);
        log.info("Customer profile created for auth user: {}", event.getAggregateId());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getProfile(UUID customerId) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId.toString()));
        return mapToResponse(c);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getProfileByAuthId(UUID authUserId) {
        Customer c = customerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", authUserId.toString()));
        return mapToResponse(c);
    }

    @Transactional
    public CustomerResponse updateProfile(UUID customerId, CustomerProfileRequest request) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId.toString()));
        if (request.getFirstName() != null) c.setFirstName(request.getFirstName());
        if (request.getLastName() != null) c.setLastName(request.getLastName());
        if (request.getEmail() != null) c.setEmail(request.getEmail());
        if (request.getDateOfBirth() != null) c.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) c.setGender(request.getGender());
        if (request.getProfileImageUrl() != null) c.setProfileImageUrl(request.getProfileImageUrl());
        return mapToResponse(customerRepository.save(c));
    }

    private CustomerResponse mapToResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .phoneNumber(c.getPhoneNumber())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .profileImageUrl(c.getProfileImageUrl())
                .totalTrips(c.getTotalTrips())
                .averageRating(c.getAverageRating())
                .build();
    }
}

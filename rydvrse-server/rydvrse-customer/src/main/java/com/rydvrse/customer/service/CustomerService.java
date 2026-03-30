package com.rydvrse.customer.service;

import com.rydvrse.customer.dto.*;
import com.rydvrse.customer.entity.Customer;
import com.rydvrse.customer.entity.CustomerAddress;
import com.rydvrse.customer.repository.CustomerAddressRepository;
import com.rydvrse.customer.repository.CustomerRepository;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;

    @Transactional(readOnly = true)
    public CustomerResponse getProfile(UUID customerId) {
        Customer customer = findById(customerId);
        return toResponse(customer);
    }

    @Transactional
    public CustomerResponse updateProfile(UUID customerId, UpdateCustomerRequest request) {
        Customer customer = findById(customerId);
        if (request.getName() != null) customer.setName(request.getName());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getProfileImageUrl() != null) customer.setProfileImageUrl(request.getProfileImageUrl());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerAddress addAddress(UUID customerId, AddAddressRequest request) {
        Customer customer = findById(customerId);
        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .label(request.getLabel())
                .address(request.getAddress())
                .lat(request.getLat())
                .lng(request.getLng())
                .build();
        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<CustomerAddress> getAddresses(UUID customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    @Transactional
    public void deleteAddress(UUID customerId, UUID addressId) {
        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId.toString()));
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Address", addressId.toString());
        }
        addressRepository.delete(address);
    }

    public Customer findById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));
    }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .profileImageUrl(c.getProfileImageUrl())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}

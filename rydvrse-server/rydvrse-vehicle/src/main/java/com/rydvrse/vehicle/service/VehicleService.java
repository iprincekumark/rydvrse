package com.rydvrse.vehicle.service;

import com.rydvrse.shared.exception.BusinessRuleException;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import com.rydvrse.vehicle.domain.Vehicle;
import com.rydvrse.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public Vehicle registerVehicle(Vehicle vehicle) {
        if (vehicleRepository.existsByRegistrationNumber(vehicle.getRegistrationNumber())) {
            throw new BusinessRuleException("Vehicle already registered: " + vehicle.getRegistrationNumber());
        }
        return vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicle(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id.toString()));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getCustomerVehicles(UUID customerId) {
        return vehicleRepository.findByOwnerId(customerId);
    }
}

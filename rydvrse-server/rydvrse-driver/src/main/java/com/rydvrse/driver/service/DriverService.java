package com.rydvrse.driver.service;

import com.rydvrse.driver.entity.*;
import com.rydvrse.driver.repository.*;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverDocumentRepository documentRepository;
    private final DriverLocationRepository locationRepository;
    private final DriverAvailabilityRepository availabilityRepository;
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional(readOnly = true)
    public Driver getProfile(UUID driverId) {
        return findById(driverId);
    }

    @Transactional
    public Driver updateProfile(UUID driverId, String name, String email, String profileImageUrl) {
        Driver driver = findById(driverId);
        if (name != null) driver.setName(name);
        if (email != null) driver.setEmail(email);
        if (profileImageUrl != null) driver.setProfileImageUrl(profileImageUrl);
        return driverRepository.save(driver);
    }

    @Transactional
    public DriverDocument uploadDocument(UUID driverId, DriverDocument document) {
        Driver driver = findById(driverId);
        document.setDriver(driver);
        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<DriverDocument> getDocuments(UUID driverId) {
        return documentRepository.findByDriverId(driverId);
    }

    @Transactional
    public DriverAvailability updateAvailability(UUID driverId, boolean isOnline) {
        DriverAvailability availability = availabilityRepository.findByDriverId(driverId)
                .orElse(DriverAvailability.builder().driverId(driverId).build());
        availability.setIsOnline(isOnline);
        if (isOnline) availability.setLastOnlineAt(Instant.now());
        return availabilityRepository.save(availability);
    }

    @Transactional
    public DriverLocation updateLocation(UUID driverId, double lat, double lng,
                                          Double heading, Double speed) {
        DriverLocation location = locationRepository.findByDriverId(driverId)
                .orElse(DriverLocation.builder().driverId(driverId).build());
        location.setPoint(geometryFactory.createPoint(new Coordinate(lng, lat)));
        location.setHeading(heading);
        location.setSpeed(speed);
        return locationRepository.save(location);
    }

    @Transactional(readOnly = true)
    public List<UUID> findNearbyDriverIds(double lat, double lng, double radiusKm) {
        return locationRepository.findNearbyDriverIds(lng, lat, radiusKm * 1000);
    }

    public Driver findById(UUID id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", id.toString()));
    }
}

package com.rydvrse.driver.service;

import com.rydvrse.auth.event.UserRegisteredEvent;
import com.rydvrse.driver.domain.Driver;
import com.rydvrse.driver.domain.DriverDocument;
import com.rydvrse.driver.event.DriverVerifiedEvent;
import com.rydvrse.driver.event.DriverAvailabilityChangedEvent;
import com.rydvrse.driver.repository.DriverRepository;
import com.rydvrse.shared.enums.DriverStatus;
import com.rydvrse.shared.enums.UserRole;
import com.rydvrse.shared.enums.VerificationStatus;
import com.rydvrse.shared.event.EventPublisher;
import com.rydvrse.shared.exception.BusinessRuleException;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Driver lifecycle service.
 * Handles: registration → document upload → KYC verification → activation → availability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final EventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.getRole() != UserRole.DRIVER) return;
        if (driverRepository.existsByPhoneNumber(event.getPhoneNumber())) return;

        Driver driver = Driver.builder()
                .authUserId(event.getAggregateId())
                .phoneNumber(event.getPhoneNumber())
                .status(DriverStatus.PENDING_VERIFICATION)
                .build();
        driverRepository.save(driver);
        log.info("Driver profile created for auth user: {}", event.getAggregateId());
    }

    @Transactional(readOnly = true)
    public Driver getDriver(UUID driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", driverId.toString()));
    }

    @Transactional(readOnly = true)
    public Driver getDriverByAuth(UUID authUserId) {
        return driverRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", authUserId.toString()));
    }

    @Transactional
    public Driver updateProfile(UUID driverId, String firstName, String lastName,
                                 String email, String licenseNumber, String operatingCity) {
        Driver driver = getDriver(driverId);
        if (firstName != null) driver.setFirstName(firstName);
        if (lastName != null) driver.setLastName(lastName);
        if (email != null) driver.setEmail(email);
        if (licenseNumber != null) driver.setLicenseNumber(licenseNumber);
        if (operatingCity != null) driver.setOperatingCity(operatingCity);
        return driverRepository.save(driver);
    }

    @Transactional
    public DriverDocument uploadDocument(UUID driverId, String docType, String docUrl, String docNumber) {
        Driver driver = getDriver(driverId);
        DriverDocument doc = DriverDocument.builder()
                .driver(driver)
                .documentType(docType)
                .documentUrl(docUrl)
                .documentNumber(docNumber)
                .verificationStatus(VerificationStatus.SUBMITTED)
                .build();
        driver.getDocuments().add(doc);
        driver.setStatus(DriverStatus.DOCUMENT_SUBMITTED);
        driverRepository.save(driver);
        log.info("Document {} uploaded for driver {}", docType, driverId);
        return doc;
    }

    /** Called by Operations module after document verification */
    @Transactional
    public void verifyDriver(UUID driverId) {
        Driver driver = getDriver(driverId);
        driver.setStatus(DriverStatus.ACTIVE);
        driverRepository.save(driver);
        eventPublisher.publish(new DriverVerifiedEvent(driverId));
        eventPublisher.publishAsync(new DriverVerifiedEvent(driverId));
        log.info("Driver {} verified and activated", driverId);
    }

    @Transactional
    public void toggleAvailability(UUID driverId, boolean available) {
        Driver driver = getDriver(driverId);
        if (driver.getStatus() != DriverStatus.ACTIVE) {
            throw new BusinessRuleException("Driver must be verified before going online");
        }
        if (driver.getIsOnTrip()) {
            throw new BusinessRuleException("Cannot change availability during an active trip");
        }
        driver.setIsAvailable(available);
        driverRepository.save(driver);
        eventPublisher.publish(new DriverAvailabilityChangedEvent(driverId, available));
        log.info("Driver {} availability: {}", driverId, available);
    }

    @Transactional
    public void updateRating(UUID driverId, double newRating) {
        Driver driver = getDriver(driverId);
        int total = driver.getTotalRatings() + 1;
        double avg = ((driver.getAverageRating() * driver.getTotalRatings()) + newRating) / total;
        driver.setAverageRating(Math.round(avg * 100.0) / 100.0);
        driver.setTotalRatings(total);
        driverRepository.save(driver);
    }

    @Transactional
    public void markOnTrip(UUID driverId, boolean onTrip) {
        Driver driver = getDriver(driverId);
        driver.setIsOnTrip(onTrip);
        if (onTrip) driver.setIsAvailable(false);
        driverRepository.save(driver);
    }
}

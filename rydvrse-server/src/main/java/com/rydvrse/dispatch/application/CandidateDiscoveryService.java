package com.rydvrse.dispatch.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.driver.domain.DriverAvailabilityStatusEntity;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverAvailabilityStatusRepository;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CandidateDiscoveryService {

    private final DriverProfileRepository driverProfileRepository;
    private final DriverAvailabilityStatusRepository driverAvailabilityStatusRepository;
    private final CandidateScoringService candidateScoringService;

    public CandidateDiscoveryService(
            DriverProfileRepository driverProfileRepository,
            DriverAvailabilityStatusRepository driverAvailabilityStatusRepository,
            CandidateScoringService candidateScoringService
    ) {
        this.driverProfileRepository = driverProfileRepository;
        this.driverAvailabilityStatusRepository = driverAvailabilityStatusRepository;
        this.candidateScoringService = candidateScoringService;
    }

    @Transactional(readOnly = true)
    public List<Candidate> discover(BookingEntity booking, int limit) {
        Map<UUID, DriverAvailabilityStatusEntity> availabilityByDriver = driverAvailabilityStatusRepository.findAll().stream()
                .collect(Collectors.toMap(DriverAvailabilityStatusEntity::getDriverProfileId, status -> status));

        return driverProfileRepository.findAll().stream()
                .filter(driver -> "APPROVED".equals(driver.getOnboardingStatus()))
                .filter(driver -> booking.getCityId().equals(driver.getDefaultCityId()))
                .map(driver -> toCandidate(booking, driver, availabilityByDriver.get(driver.getId())))
                .sorted(Comparator.comparing(Candidate::score).reversed())
                .limit(limit)
                .toList();
    }

    private Candidate toCandidate(BookingEntity booking, DriverProfileEntity driver, DriverAvailabilityStatusEntity availability) {
        BigDecimal score = candidateScoringService.score(booking, driver, availability);
        String availabilityState = availability == null ? "UNKNOWN" : availability.getCurrentStatus();
        int etaMinutes = switch (availabilityState) {
            case "ONLINE" -> 8;
            case "IDLE" -> 12;
            case "OFFLINE" -> 20;
            default -> 15;
        };
        return new Candidate(
                driver.getId(),
                (driver.getFirstName() + " " + (driver.getLastName() == null ? "" : driver.getLastName())).trim(),
                availabilityState,
                score,
                etaMinutes
        );
    }

    public record Candidate(
            UUID driverId,
            String fullName,
            String availabilityState,
            BigDecimal score,
            int etaMinutes
    ) {
    }
}

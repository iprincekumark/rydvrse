package com.rydvrse.dispatch.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.driver.domain.DriverAvailabilityStatusEntity;
import com.rydvrse.driver.domain.DriverProfileEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CandidateScoringService {

    public BigDecimal score(BookingEntity booking, DriverProfileEntity driver, DriverAvailabilityStatusEntity availability) {
        double score = 0.0;
        if (driver.getDefaultCityId() != null && driver.getDefaultCityId().equals(booking.getCityId())) {
            score += 55;
        }
        if ("APPROVED".equals(driver.getOnboardingStatus())) {
            score += 20;
        }
        if ("CLEAR".equalsIgnoreCase(driver.getComplianceStatus()) || "ACTIVE".equalsIgnoreCase(driver.getComplianceStatus())) {
            score += 10;
        }
        if (availability != null) {
            score += switch (availability.getCurrentStatus()) {
                case "ONLINE" -> 15;
                case "IDLE" -> 12;
                case "OFFLINE" -> 0;
                default -> 5;
            };
        }
        if (driver.getRatingAvg() != null) {
            score += Math.min(5, driver.getRatingAvg().doubleValue());
        }
        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);
    }
}

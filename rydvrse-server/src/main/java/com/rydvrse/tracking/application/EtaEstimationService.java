package com.rydvrse.tracking.application;

import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.trip.domain.LocationPingEntity;
import com.rydvrse.trip.domain.TripEntity;
import org.springframework.stereotype.Service;

@Service
public class EtaEstimationService {

    public Integer estimateMinutes(TripEntity trip, AssignmentEntity assignment, LocationPingEntity latestPing) {
        if (assignment != null && assignment.getDriverEtaSeconds() != null && trip.getStartedAt() == null) {
            return Math.max(1, assignment.getDriverEtaSeconds() / 60);
        }
        if (latestPing != null && trip.getStartedAt() != null) {
            return 8;
        }
        if ("COMPLETED".equals(trip.getStatus())) {
            return 0;
        }
        return 10;
    }
}

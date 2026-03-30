package com.rydvrse.safety.service;

import com.rydvrse.safety.entity.*;
import com.rydvrse.safety.repository.*;
import com.rydvrse.shared.enums.*;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class SafetyService {
    private final SosAlertRepository sosRepository;
    private final IncidentRepository incidentRepository;
    private final TripShareLinkRepository shareRepository;

    @Transactional
    public SosAlert triggerSos(UUID tripId, UUID userId, UserType userType, double lat, double lng) {
        SosAlert sos = SosAlert.builder()
                .tripId(tripId).triggeredBy(userType).triggeredById(userId)
                .lat(lat).lng(lng).status(SosStatus.ACTIVE).build();
        log.warn("SOS ALERT triggered for trip {} by {} {}", tripId, userType, userId);
        return sosRepository.save(sos);
    }

    @Transactional
    public SosAlert resolveSos(UUID sosId, UUID resolvedBy, String notes) {
        SosAlert sos = sosRepository.findById(sosId)
                .orElseThrow(() -> new ResourceNotFoundException("SosAlert", sosId.toString()));
        sos.setStatus(SosStatus.RESOLVED);
        sos.setResolvedAt(Instant.now());
        sos.setResolvedBy(resolvedBy);
        sos.setNotes(notes);
        return sosRepository.save(sos);
    }

    @Transactional(readOnly = true)
    public List<SosAlert> getActiveSosAlerts() {
        return sosRepository.findByStatus(SosStatus.ACTIVE);
    }

    @Transactional
    public Incident reportIncident(UUID tripId, UUID reporterId, UserType reportedBy,
                                    IncidentType type, String description, IncidentPriority priority) {
        Incident incident = Incident.builder()
                .tripId(tripId).reporterId(reporterId).reportedBy(reportedBy)
                .type(type).description(description).priority(priority).build();
        return incidentRepository.save(incident);
    }

    @Transactional(readOnly = true)
    public Incident getIncident(UUID incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId.toString()));
    }

    @Transactional
    public TripShareLink shareTrip(UUID tripId, UUID customerId, String recipientPhone, String recipientName) {
        TripShareLink link = TripShareLink.builder()
                .tripId(tripId).customerId(customerId)
                .recipientPhone(recipientPhone).recipientName(recipientName)
                .expiresAt(Instant.now().plusSeconds(86400)).build();
        return shareRepository.save(link);
    }
}

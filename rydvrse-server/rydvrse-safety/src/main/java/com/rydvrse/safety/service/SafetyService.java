package com.rydvrse.safety.service;

import com.rydvrse.safety.domain.SafetyIncident;
import com.rydvrse.safety.repository.SafetyIncidentRepository;
import com.rydvrse.shared.domain.GeoLocation;
import com.rydvrse.shared.event.EventPublisher;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * Safety service — handles emergency SOS, incident reporting, and safety monitoring.
 * In production: integrates with local police API and emergency contacts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyService {

    private final SafetyIncidentRepository incidentRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public SafetyIncident reportSos(UUID tripId, UUID reporterId, String reporterType,
                                     double lat, double lng) {
        SafetyIncident incident = SafetyIncident.builder()
                .tripId(tripId).reportedBy(reporterId).reporterType(reporterType)
                .incidentType("SOS").description("Emergency SOS triggered")
                .location(GeoLocation.builder().latitude(lat).longitude(lng).build())
                .priority("CRITICAL").build();
        SafetyIncident saved = incidentRepository.save(incident);
        log.error("[SOS ALERT] Emergency SOS for trip {} at [{}, {}]", tripId, lat, lng);
        // In production: alert emergency contacts, notify operations team, alert police
        return saved;
    }

    @Transactional
    public SafetyIncident reportIncident(SafetyIncident incident) {
        SafetyIncident saved = incidentRepository.save(incident);
        log.warn("[SAFETY] Incident reported: {} for trip {}", saved.getIncidentType(), saved.getTripId());
        return saved;
    }

    @Transactional
    public SafetyIncident resolveIncident(UUID incidentId, String notes) {
        SafetyIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("SafetyIncident", incidentId.toString()));
        incident.setStatus("RESOLVED");
        incident.setResolutionNotes(notes);
        return incidentRepository.save(incident);
    }

    @Transactional(readOnly = true)
    public List<SafetyIncident> getActiveIncidents() {
        return incidentRepository.findByStatusOrderByCreatedAtDesc("REPORTED");
    }
}

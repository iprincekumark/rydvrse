package com.rydvrse.driver.application;

import com.rydvrse.driver.domain.DriverDocumentEntity;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DriverDocumentService {

    private final DriverProfileService driverProfileService;
    private final DriverDocumentRepository driverDocumentRepository;

    public DriverDocumentService(DriverProfileService driverProfileService, DriverDocumentRepository driverDocumentRepository) {
        this.driverProfileService = driverProfileService;
        this.driverDocumentRepository = driverDocumentRepository;
    }

    @Transactional
    public Map<String, Object> create(DocumentCommand command) {
        DriverProfileEntity profile = driverProfileService.requireCurrentProfile();
        DriverDocumentEntity entity = new DriverDocumentEntity();
        entity.setDriverProfileId(profile.getId());
        entity.setDocumentType(command.documentType());
        entity.setDocumentNumberMasked(command.documentNumber());
        entity.setStorageKey(command.mediaAssetId());
        entity.setStatus("UPLOADED");
        entity.setExpiresAt(command.expiresOn());
        entity.setSubmittedAt(OffsetDateTime.now());
        driverDocumentRepository.save(entity);
        return toMap(entity);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list() {
        DriverProfileEntity profile = driverProfileService.requireCurrentProfile();
        return driverDocumentRepository.findByDriverProfileIdOrderBySubmittedAtDesc(profile.getId()).stream()
                .map(this::toMap)
                .toList();
    }

    private Map<String, Object> toMap(DriverDocumentEntity entity) {
        return Map.of(
                "document_id", entity.getId(),
                "document_type", entity.getDocumentType(),
                "media_asset_id", entity.getStorageKey(),
                "verification_state", entity.getStatus(),
                "expires_on", entity.getExpiresAt()
        );
    }

    public record DocumentCommand(String documentType, String mediaAssetId, String documentNumber, LocalDate expiresOn) {
    }
}

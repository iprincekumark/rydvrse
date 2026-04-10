package com.rydvrse.common.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.common.security.RydvrsePrincipal;
import com.rydvrse.common.util.RequestIdHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final JsonNodeUtils jsonNodeUtils;
    private final CurrentActorService currentActorService;

    public AuditService(
            AuditLogRepository auditLogRepository,
            JsonNodeUtils jsonNodeUtils,
            CurrentActorService currentActorService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.jsonNodeUtils = jsonNodeUtils;
        this.currentActorService = currentActorService;
    }

    public void record(String actionType, String entityType, UUID entityId, Object oldValue, Object newValue, String reasonCode, String note) {
        AuditLogEntity entity = new AuditLogEntity();
        RydvrsePrincipal principal = currentActorService.getCurrentActor().orElse(null);
        entity.setActorUserId(principal == null ? null : principal.userId());
        entity.setActorRoleCode(principal == null || principal.roles().isEmpty() ? null : principal.roles().getFirst());
        entity.setActionType(actionType);
        entity.setEntityType(entityType);
        entity.setEntityId(entityId);
        entity.setOldValue(toJson(oldValue));
        entity.setNewValue(toJson(newValue));
        entity.setReasonCode(reasonCode);
        entity.setNote(note);
        entity.setRequestId(RequestIdHolder.get());
        entity.setSourceSystem("API");
        auditLogRepository.save(entity);
    }

    private JsonNode toJson(Object value) {
        return value == null ? null : jsonNodeUtils.toJsonNode(value);
    }
}

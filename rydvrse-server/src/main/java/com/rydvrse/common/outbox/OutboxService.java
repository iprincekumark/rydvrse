package com.rydvrse.common.outbox;

import com.rydvrse.common.persistence.JsonNodeUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonNodeUtils jsonNodeUtils;

    public OutboxService(OutboxEventRepository outboxEventRepository, JsonNodeUtils jsonNodeUtils) {
        this.outboxEventRepository = outboxEventRepository;
        this.jsonNodeUtils = jsonNodeUtils;
    }

    public void publish(String aggregateType, UUID aggregateId, String eventType, Object payload) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setAggregateType(aggregateType);
        entity.setAggregateId(aggregateId);
        entity.setEventType(eventType);
        entity.setPayload(jsonNodeUtils.toJsonNode(payload));
        entity.setOutboxStatus("PENDING");
        entity.setAvailableAt(OffsetDateTime.now());
        entity.setRetryCount(0);
        outboxEventRepository.save(entity);
    }
}

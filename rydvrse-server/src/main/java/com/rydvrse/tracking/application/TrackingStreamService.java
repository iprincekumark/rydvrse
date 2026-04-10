package com.rydvrse.tracking.application;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@Service
public class TrackingStreamService {

    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID tripId, Supplier<Map<String, Object>> snapshotSupplier) {
        SseEmitter emitter = new SseEmitter(120_000L);
        emitters.computeIfAbsent(tripId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(tripId, emitter));
        emitter.onTimeout(() -> remove(tripId, emitter));
        emitter.onError(error -> remove(tripId, emitter));
        try {
            emitter.send(SseEmitter.event()
                    .name("trip.location_updated")
                    .data(Map.of(
                            "event_type", "trip.location_updated",
                            "occurred_at", OffsetDateTime.now(),
                            "payload", snapshotSupplier.get()
                    )));
        } catch (IOException ex) {
            emitter.completeWithError(ex);
        }
        return emitter;
    }

    public void publish(UUID tripId, String eventType, Map<String, Object> payload) {
        for (SseEmitter emitter : emitters.getOrDefault(tripId, new CopyOnWriteArrayList<>())) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(Map.of(
                                "event_type", eventType,
                                "occurred_at", OffsetDateTime.now(),
                                "payload", payload
                        )));
            } catch (IOException ex) {
                remove(tripId, emitter);
            }
        }
    }

    private void remove(UUID tripId, SseEmitter emitter) {
        emitters.computeIfPresent(tripId, (ignored, existing) -> {
            existing.remove(emitter);
            return existing.isEmpty() ? null : existing;
        });
    }
}

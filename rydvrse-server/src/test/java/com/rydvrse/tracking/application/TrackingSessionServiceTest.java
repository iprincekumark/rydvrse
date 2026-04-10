package com.rydvrse.tracking.application;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.trip.domain.TrackingSessionEntity;
import com.rydvrse.trip.infrastructure.TrackingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingSessionServiceTest {

    @Mock
    private TrackingSessionRepository trackingSessionRepository;

    @Test
    void markStaleSessionsShouldMoveActiveSessionsToDegraded() {
        RydvrseProperties properties = new RydvrseProperties();
        properties.getTracking().setStaleThresholdSeconds(60);
        TrackingSessionService service = new TrackingSessionService(trackingSessionRepository, properties);

        TrackingSessionEntity stale = new TrackingSessionEntity();
        stale.setStatus("ACTIVE");
        stale.setLastPingAt(OffsetDateTime.now().minusMinutes(5));

        when(trackingSessionRepository.findByStatusAndLastPingAtBefore(org.mockito.ArgumentMatchers.eq("ACTIVE"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(stale));
        doAnswer(invocation -> invocation.getArgument(0)).when(trackingSessionRepository).saveAll(anyList());

        int marked = service.markStaleSessions();

        assertThat(marked).isEqualTo(1);
        assertThat(stale.getStatus()).isEqualTo("DEGRADED");
    }
}

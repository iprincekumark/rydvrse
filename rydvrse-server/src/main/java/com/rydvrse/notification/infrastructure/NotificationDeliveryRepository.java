package com.rydvrse.notification.infrastructure;

import com.rydvrse.notification.domain.NotificationDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDeliveryEntity, UUID> {

    List<NotificationDeliveryEntity> findTop50ByStatusOrderByQueuedAtAsc(String status);

    List<NotificationDeliveryEntity> findTop50ByStatusAndQueuedAtBeforeOrderByQueuedAtAsc(String status, OffsetDateTime queuedBefore);
}

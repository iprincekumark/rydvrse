package com.rydvrse.notification.infrastructure;

import com.rydvrse.notification.domain.NotificationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationEventRepository extends JpaRepository<NotificationEventEntity, UUID> {
}

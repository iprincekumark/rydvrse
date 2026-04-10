package com.rydvrse.notification.infrastructure;

import com.rydvrse.notification.domain.NotificationTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, UUID> {

    Optional<NotificationTemplateEntity> findByTemplateCodeAndChannelAndLanguageCodeAndActiveTrue(String templateCode, String channel, String languageCode);
}

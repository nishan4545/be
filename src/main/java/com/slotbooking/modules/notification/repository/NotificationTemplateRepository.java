package com.slotbooking.modules.notification.repository;

import com.slotbooking.modules.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for NotificationTemplate entity operations.
 */
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByTemplateName(String templateName);
}

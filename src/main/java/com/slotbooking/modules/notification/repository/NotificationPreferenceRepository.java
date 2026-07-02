package com.slotbooking.modules.notification.repository;

import com.slotbooking.modules.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for NotificationPreference entity operations.
 */
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    Optional<NotificationPreference> findByUserId(Long userId);
}

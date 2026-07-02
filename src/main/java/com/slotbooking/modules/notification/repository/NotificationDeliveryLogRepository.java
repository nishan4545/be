package com.slotbooking.modules.notification.repository;

import com.slotbooking.modules.notification.entity.NotificationDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for NotificationDeliveryLog entity operations.
 */
public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, UUID> {

    List<NotificationDeliveryLog> findByNotificationId(UUID notificationId);

    @Modifying
    @Query("DELETE FROM NotificationDeliveryLog l WHERE l.createdAt < :threshold")
    void deleteLogsOlderThan(@Param("threshold") LocalDateTime threshold);
}

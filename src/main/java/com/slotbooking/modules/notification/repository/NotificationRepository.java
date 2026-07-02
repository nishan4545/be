package com.slotbooking.modules.notification.repository;

import com.slotbooking.modules.notification.entity.Notification;
import com.slotbooking.modules.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Notification entity operations.
 */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndStatus(Long userId, NotificationStatus status);

    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByStatusIn(List<NotificationStatus> statuses);

    List<Notification> findByStatusAndCreatedAtBefore(NotificationStatus status, LocalDateTime threshold);

    long countByStatus(NotificationStatus status);
}

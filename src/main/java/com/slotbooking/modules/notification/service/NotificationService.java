package com.slotbooking.modules.notification.service;

import com.slotbooking.modules.notification.dto.BroadcastRequest;
import com.slotbooking.modules.notification.dto.NotificationResponse;
import com.slotbooking.modules.notification.dto.NotificationStatsResponse;
import com.slotbooking.modules.notification.entity.Notification;
import com.slotbooking.modules.notification.enums.NotificationPriority;
import com.slotbooking.modules.notification.enums.NotificationType;
import com.slotbooking.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Core notification service managing creation, private routing check, history lookup, and admin broadcasts.
 */
public interface NotificationService {

    /**
     * Creates and saves a notification record.
     */
    NotificationResponse createNotification(User user, String title, String message, 
                                            NotificationType type, String deliveryChannel, 
                                            NotificationPriority priority, String refType, Long refId);

    /**
     * Dispatches notification delivery to targeted providers asynchronously.
     */
    void sendNotification(Notification notification);

    /**
     * Marks a specific notification as read.
     */
    void markAsRead(User currentUser, UUID notificationId);

    /**
     * Marks all unread notifications of the player as read.
     */
    void markAllAsRead(User currentUser);

    /**
     * Deletes a player notification record.
     */
    void deleteNotification(User currentUser, UUID notificationId);

    /**
     * Gets the count of unread notifications for a player.
     */
    long getUnreadCount(User currentUser);

    /**
     * Gets the notification page history for the player.
     */
    Page<NotificationResponse> getNotificationHistory(User currentUser, Pageable pageable);

    /**
     * Lists all notifications in the system (Admin only).
     */
    Page<NotificationResponse> getAllNotifications(Pageable pageable);

    /**
     * Gathers stats metrics for all notifications (Admin only).
     */
    NotificationStatsResponse getStatistics();

    /**
     * Lists all notifications that failed delivery (Admin only).
     */
    List<NotificationResponse> getFailedNotifications();

    /**
     * Broadcasts an announcement alert to all users (Admin only).
     */
    void broadcastAnnouncement(BroadcastRequest request);
}

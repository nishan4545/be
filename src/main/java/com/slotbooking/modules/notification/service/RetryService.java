package com.slotbooking.modules.notification.service;

import java.util.UUID;

/**
 * Service interface managing failed notification delivery retries.
 */
public interface RetryService {

    /**
     * Attempts to re-deliver a specific failed notification.
     *
     * @param notificationId the notification identifier to retry
     */
    void retryNotification(UUID notificationId);

    /**
     * Scheduled job entry point to auto-retry all notifications currently in PENDING/FAILED status
     * under the configured maximum retry threshold.
     */
    void retryFailedNotifications();
}

package com.slotbooking.modules.notification.service;

import com.slotbooking.exception.ResourceNotFoundException;
import com.slotbooking.modules.notification.entity.Notification;
import com.slotbooking.modules.notification.entity.NotificationDeliveryLog;
import com.slotbooking.modules.notification.enums.NotificationStatus;
import com.slotbooking.modules.notification.repository.NotificationDeliveryLogRepository;
import com.slotbooking.modules.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for RetryService.
 * Controls auto-retries and manual triggers for failed messages.
 */
@Service
@Slf4j
public class RetryServiceImpl implements RetryService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryLogRepository logRepository;
    private final NotificationService notificationService;
    private final int retryLimit;

    public RetryServiceImpl(
            NotificationRepository notificationRepository,
            NotificationDeliveryLogRepository logRepository,
            @Lazy NotificationService notificationService,
            @Value("${NOTIFICATION_RETRY_LIMIT:5}") int retryLimit
    ) {
        this.notificationRepository = notificationRepository;
        this.logRepository = logRepository;
        this.notificationService = notificationService;
        this.retryLimit = retryLimit;
    }

    @Override
    @Transactional
    public void retryNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        log.info("[Retry Service] Manual retry triggered for notification: {}", notificationId);
        notification.setStatus(NotificationStatus.PENDING);
        notificationRepository.save(notification);

        notificationService.sendNotification(notification);
    }

    @Override
    @Transactional
    public void retryFailedNotifications() {
        log.info("[Retry Service] Automated retry check started. Limit: {}", retryLimit);
        List<Notification> failedNotifications = notificationRepository.findByStatus(NotificationStatus.FAILED);
        
        for (Notification notification : failedNotifications) {
            List<NotificationDeliveryLog> logs = logRepository.findByNotificationId(notification.getId());
            int maxRetriesSoFar = logs.stream()
                    .mapToInt(NotificationDeliveryLog::getRetryCount)
                    .max()
                    .orElse(0);

            if (maxRetriesSoFar < retryLimit) {
                log.info("[Retry Service] Retrying notification ID: {}, attempt: {}", notification.getId(), maxRetriesSoFar + 1);
                notification.setStatus(NotificationStatus.PENDING);
                notificationRepository.save(notification);
                notificationService.sendNotification(notification);
            } else {
                log.warn("[Retry Service] Notification ID: {} has hit the max retry limit ({}). Skipping.", 
                        notification.getId(), retryLimit);
                notification.setStatus(NotificationStatus.EXPIRED);
                notificationRepository.save(notification);
            }
        }
    }
}

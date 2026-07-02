package com.slotbooking.modules.notification.service;

import com.slotbooking.exception.BusinessException;
import com.slotbooking.exception.ResourceNotFoundException;
import com.slotbooking.modules.notification.dto.BroadcastRequest;
import com.slotbooking.modules.notification.dto.NotificationResponse;
import com.slotbooking.modules.notification.dto.NotificationStatsResponse;
import com.slotbooking.modules.notification.entity.Notification;
import com.slotbooking.modules.notification.entity.NotificationDeliveryLog;
import com.slotbooking.modules.notification.entity.NotificationPreference;
import com.slotbooking.modules.notification.enums.DeliveryChannel;
import com.slotbooking.modules.notification.enums.NotificationPriority;
import com.slotbooking.modules.notification.enums.NotificationStatus;
import com.slotbooking.modules.notification.enums.NotificationType;
import com.slotbooking.modules.notification.repository.NotificationDeliveryLogRepository;
import com.slotbooking.modules.notification.repository.NotificationRepository;
import com.slotbooking.modules.notification.repository.NotificationPreferenceRepository;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core notification service implementation.
 * Manages database notifications, user channel checks, and async alerts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationDeliveryLogRepository logRepository;
    private final UserRepository userRepository;

    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;
    private final org.springframework.messaging.simp.SimpMessageSendingOperations messagingTemplate;

    @Override
    @Transactional
    public NotificationResponse createNotification(User user, String title, String message, 
                                            NotificationType type, String deliveryChannel, 
                                            NotificationPriority priority, String refType, Long refId) {
        
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .notificationType(type)
                .deliveryChannel(DeliveryChannel.valueOf(deliveryChannel.toUpperCase()))
                .status(NotificationStatus.PENDING)
                .priority(priority)
                .referenceType(refType)
                .referenceId(refId)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("[Notification Service] Created notification ID: {} for user: {}", saved.getId(), user.getMobileNumber());

        // Dispatch asynchronously
        sendNotification(saved);

        return mapToResponse(saved);
    }

    @Async
    @Override
    @Transactional
    public void sendNotification(Notification notification) {
        log.info("[Notification Service] Dispatching notification ID: {} via channel: {}", 
                notification.getId(), notification.getDeliveryChannel());

        // 1. Load preferences
        NotificationPreference preference = preferenceRepository.findByUserId(notification.getUser().getId())
                .orElseGet(() -> createDefaultPreference(notification.getUser()));

        // 2. Validate channel eligibility (CRITICAL priority bypasses preferences)
        boolean isCritical = notification.getPriority() == NotificationPriority.CRITICAL;
        boolean isChannelEnabled = isCritical;

        switch (notification.getDeliveryChannel()) {
            case EMAIL -> isChannelEnabled = isCritical || preference.isEmailEnabled();
            case PUSH -> isChannelEnabled = isCritical || preference.isPushEnabled();
            case SMS -> isChannelEnabled = isCritical || preference.isSmsEnabled();
            case WEBSOCKET -> isChannelEnabled = isCritical || preference.isWebsocketEnabled();
            case IN_APP -> isChannelEnabled = isCritical || preference.isInAppEnabled();
        }

        if (!isChannelEnabled) {
            log.info("[Notification Service] Channel {} disabled for user ID: {}. Expirying alert.", 
                    notification.getDeliveryChannel(), notification.getUser().getId());
            notification.setStatus(NotificationStatus.EXPIRED);
            notificationRepository.save(notification);
            return;
        }

        // 3. Process dispatch by channel
        try {
            switch (notification.getDeliveryChannel()) {
                case IN_APP -> {
                    notification.setStatus(NotificationStatus.DELIVERED);
                    notification.setDeliveredAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    writeLog(notification, DeliveryChannel.IN_APP, "DATABASE", NotificationStatus.DELIVERED, "In-app stored");
                }
                case WEBSOCKET -> {
                    String destination = "/queue/notifications";
                    log.info("[Notification Service] WebSocket push to: {}", notification.getUser().getMobileNumber());
                    messagingTemplate.convertAndSendToUser(
                            notification.getUser().getMobileNumber(), 
                            destination, 
                            mapToResponse(notification)
                    );
                    notification.setStatus(NotificationStatus.DELIVERED);
                    notification.setDeliveredAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    writeLog(notification, DeliveryChannel.WEBSOCKET, "SPRING_STOMP", NotificationStatus.DELIVERED, "WebSocket pushed");
                }
                case EMAIL -> {
                    if (notification.getUser().getEmail() == null || notification.getUser().getEmail().isBlank()) {
                        throw new BusinessException("User has no registered email address.");
                    }
                    emailService.sendEmail(notification.getUser().getEmail(), notification.getTitle(), notification.getMessage());
                    notification.setStatus(NotificationStatus.DELIVERED);
                    notification.setDeliveredAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    writeLog(notification, DeliveryChannel.EMAIL, "SMTP", NotificationStatus.DELIVERED, "Email dispatched");
                }
                case SMS -> {
                    smsService.sendSms(notification.getUser().getMobileNumber(), notification.getMessage());
                    notification.setStatus(NotificationStatus.DELIVERED);
                    notification.setDeliveredAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    writeLog(notification, DeliveryChannel.SMS, "MSG91", NotificationStatus.DELIVERED, "SMS dispatched");
                }
                case PUSH -> {
                    pushNotificationService.sendPushNotification(
                            notification.getUser().getId(), 
                            notification.getTitle(), 
                            notification.getMessage()
                    );
                    notification.setStatus(NotificationStatus.DELIVERED);
                    notification.setDeliveredAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    writeLog(notification, DeliveryChannel.PUSH, "FCM", NotificationStatus.DELIVERED, "Push dispatched");
                }
            }
        } catch (Exception e) {
            log.error("[Notification Service] Delivery failed for notification: {}, error: {}", 
                    notification.getId(), e.getMessage());
            
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            
            writeLog(notification, notification.getDeliveryChannel(), "GATEWAY", NotificationStatus.FAILED, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void markAsRead(User currentUser, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied.");
        }

        if (notification.getStatus() != NotificationStatus.READ) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("[Notification Service] Notification {} marked as read", notificationId);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(User currentUser) {
        log.info("[Notification Service] Marking all unread alerts as read for user: {}", currentUser.getMobileNumber());
        List<Notification> unread = notificationRepository.findByUserIdAndStatus(currentUser.getId(), NotificationStatus.DELIVERED);
        
        for (Notification notification : unread) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(User currentUser, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied.");
        }

        notificationRepository.delete(notification);
        log.info("[Notification Service] Notification {} deleted.", notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(User currentUser) {
        return notificationRepository.countByUserIdAndStatus(currentUser.getId(), NotificationStatus.DELIVERED);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationHistory(User currentUser, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatsResponse getStatistics() {
        return NotificationStatsResponse.builder()
                .totalNotifications(notificationRepository.count())
                .pendingNotifications(notificationRepository.countByStatus(NotificationStatus.PENDING))
                .sentNotifications(notificationRepository.countByStatus(NotificationStatus.SENT))
                .deliveredNotifications(notificationRepository.countByStatus(NotificationStatus.DELIVERED))
                .failedNotifications(notificationRepository.countByStatus(NotificationStatus.FAILED))
                .readNotifications(notificationRepository.countByStatus(NotificationStatus.READ))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getFailedNotifications() {
        return notificationRepository.findByStatus(NotificationStatus.FAILED).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void broadcastAnnouncement(BroadcastRequest request) {
        log.info("[Notification Service] Global broadcast triggered: {}", request.getTitle());
        List<User> players = userRepository.findByRole(Role.PLAYER);
        
        for (User player : players) {
            createNotification(
                    player,
                    request.getTitle(),
                    request.getMessage(),
                    NotificationType.ADMIN_BROADCAST,
                    DeliveryChannel.IN_APP.name(),
                    NotificationPriority.NORMAL,
                    null,
                    null
            );
            
            // Push websocket notification
            createNotification(
                    player,
                    request.getTitle(),
                    request.getMessage(),
                    NotificationType.ADMIN_BROADCAST,
                    DeliveryChannel.WEBSOCKET.name(),
                    NotificationPriority.NORMAL,
                    null,
                    null
            );
        }
    }

    // Helper methods

    private NotificationPreference createDefaultPreference(User user) {
        NotificationPreference preference = NotificationPreference.builder()
                .user(user)
                .emailEnabled(true)
                .pushEnabled(true)
                .smsEnabled(true)
                .websocketEnabled(true)
                .inAppEnabled(true)
                .build();
        return preferenceRepository.save(preference);
    }

    private void writeLog(Notification notification, DeliveryChannel channel, String provider, NotificationStatus status, String response) {
        List<NotificationDeliveryLog> logs = logRepository.findByNotificationId(notification.getId());
        int retryCount = logs.stream()
                .filter(l -> l.getChannel() == channel)
                .mapToInt(NotificationDeliveryLog::getRetryCount)
                .max()
                .orElse(-1) + 1;

        NotificationDeliveryLog logEntity = NotificationDeliveryLog.builder()
                .notification(notification)
                .channel(channel)
                .provider(provider)
                .status(status)
                .response(response)
                .retryCount(retryCount)
                .build();
        logRepository.save(logEntity);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType().name())
                .deliveryChannel(notification.getDeliveryChannel().name())
                .status(notification.getStatus().name())
                .priority(notification.getPriority().name())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}

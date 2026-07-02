package com.slotbooking.scheduler;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.notification.entity.Notification;
import com.slotbooking.modules.notification.enums.DeliveryChannel;
import com.slotbooking.modules.notification.enums.NotificationPriority;
import com.slotbooking.modules.notification.enums.NotificationStatus;
import com.slotbooking.modules.notification.enums.NotificationType;
import com.slotbooking.modules.notification.repository.NotificationRepository;
import com.slotbooking.modules.notification.scheduler.NotificationScheduler;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
public class SchedulerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationScheduler notificationScheduler;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCleanupExpiredNotificationsScheduler() {
        User user = User.builder()
                .mobileNumber("9876543277")
                .password("pass")
                .fullName("SchedulerUser")
                .mobileVerified(true)
                .build();
        user = userRepository.save(user);

        // Save a pending notification created 2 hours ago
        Notification notification = Notification.builder()
                .user(user)
                .title("Stale Alert")
                .message("Message")
                .notificationType(NotificationType.ADMIN_BROADCAST)
                .deliveryChannel(DeliveryChannel.IN_APP)
                .status(NotificationStatus.PENDING)
                .priority(NotificationPriority.NORMAL)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        notification = notificationRepository.save(notification);

        // Run the cleanup scheduler job manually
        notificationScheduler.runCleanupJob();

        // Verify status transitioned to EXPIRED
        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertEquals(NotificationStatus.EXPIRED, updated.getStatus());
    }
}

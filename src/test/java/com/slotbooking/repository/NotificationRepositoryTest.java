package com.slotbooking.repository;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.notification.entity.Notification;
import com.slotbooking.modules.notification.enums.DeliveryChannel;
import com.slotbooking.modules.notification.enums.NotificationPriority;
import com.slotbooking.modules.notification.enums.NotificationStatus;
import com.slotbooking.modules.notification.enums.NotificationType;
import com.slotbooking.modules.notification.repository.NotificationRepository;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class NotificationRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Notification notification;

    @BeforeEach
    void setup() {
        user = User.builder()
                .mobileNumber("9876543255")
                .password("pass")
                .fullName("Charlie")
                .mobileVerified(true)
                .build();
        user = userRepository.save(user);

        notification = Notification.builder()
                .user(user)
                .title("Notice")
                .message("Test notice")
                .notificationType(NotificationType.ADMIN_BROADCAST)
                .deliveryChannel(DeliveryChannel.IN_APP)
                .status(NotificationStatus.PENDING)
                .priority(NotificationPriority.NORMAL)
                .build();
        notification = notificationRepository.save(notification);
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc() {
        Page<Notification> history = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 10));
        assertEquals(1, history.getTotalElements());
        assertEquals("Notice", history.getContent().getFirst().getTitle());
    }

    @Test
    void testCountByUserIdAndStatus() {
        long count = notificationRepository.countByUserIdAndStatus(user.getId(), NotificationStatus.PENDING);
        assertEquals(1, count);
    }

    @Test
    void testFindByStatus() {
        List<Notification> list = notificationRepository.findByStatus(NotificationStatus.PENDING);
        assertFalse(list.isEmpty());
    }
}

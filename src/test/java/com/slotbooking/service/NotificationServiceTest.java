package com.slotbooking.service;

import com.slotbooking.modules.notification.dto.NotificationResponse;
import com.slotbooking.modules.notification.entity.Notification;
import com.slotbooking.modules.notification.entity.NotificationPreference;
import com.slotbooking.modules.notification.enums.DeliveryChannel;
import com.slotbooking.modules.notification.enums.NotificationPriority;
import com.slotbooking.modules.notification.enums.NotificationStatus;
import com.slotbooking.modules.notification.enums.NotificationType;
import com.slotbooking.modules.notification.repository.NotificationDeliveryLogRepository;
import com.slotbooking.modules.notification.repository.NotificationPreferenceRepository;
import com.slotbooking.modules.notification.repository.NotificationRepository;
import com.slotbooking.modules.notification.service.NotificationServiceImpl;
import com.slotbooking.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private NotificationDeliveryLogRepository logRepository;

    @Mock
    private com.slotbooking.modules.user.repository.UserRepository userRepository;

    @Mock
    private com.slotbooking.modules.notification.service.EmailService emailService;

    @Mock
    private com.slotbooking.modules.notification.service.SmsService smsService;

    @Mock
    private com.slotbooking.modules.notification.service.PushNotificationService pushNotificationService;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User playerUser;
    private Notification emailNotification;
    private NotificationPreference optOutPreference;

    @BeforeEach
    void setUp() {
        playerUser = User.builder()
                .id(1L)
                .mobileNumber("9876543210")
                .email("test@player.com")
                .build();

        emailNotification = Notification.builder()
                .id(UUID.randomUUID())
                .user(playerUser)
                .title("Match Schedule")
                .message("You have a match tomorrow!")
                .notificationType(NotificationType.TOURNAMENT_REMINDER)
                .deliveryChannel(DeliveryChannel.EMAIL)
                .priority(NotificationPriority.NORMAL)
                .status(NotificationStatus.PENDING)
                .build();

        optOutPreference = NotificationPreference.builder()
                .user(playerUser)
                .emailEnabled(false) // Opt-out
                .build();
    }

    @Test
    void sendNotification_optedOut_marksExpired() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(optOutPreference));

        notificationService.sendNotification(emailNotification);
        assertEquals(NotificationStatus.EXPIRED, emailNotification.getStatus());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendNotification_criticalBypassesOptOut_sendsSuccessfully() {
        emailNotification.setPriority(NotificationPriority.CRITICAL);
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(optOutPreference));

        notificationService.sendNotification(emailNotification);
        assertEquals(NotificationStatus.DELIVERED, emailNotification.getStatus());
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }
}

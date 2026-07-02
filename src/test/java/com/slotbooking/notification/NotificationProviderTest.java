package com.slotbooking.notification;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.notification.provider.EmailProvider;
import com.slotbooking.modules.notification.provider.PushNotificationProvider;
import com.slotbooking.modules.notification.provider.SmsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class NotificationProviderTest extends BaseIntegrationTest {

    @Autowired
    private EmailProvider emailProvider;

    @Autowired
    private SmsProvider smsProvider;

    @Autowired
    private PushNotificationProvider pushNotificationProvider;

    @Test
    void testProvidersMockFallbacksSuccess() {
        // Verifies fallback mock delivery flows when environment credentials are blank.
        assertDoesNotThrow(() -> emailProvider.sendEmail("mock@test.com", "Test Title", "HTML Body"));
        assertDoesNotThrow(() -> smsProvider.sendSms("9876543210", "Mock SMS Alert"));
        assertDoesNotThrow(() -> pushNotificationProvider.sendPush("mock_device_token", "Push Title", "Push Body"));
    }
}

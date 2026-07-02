package com.slotbooking.modules.notification.service;

import com.slotbooking.modules.notification.dto.DeviceTokenRequest;
import com.slotbooking.modules.user.entity.User;

/**
 * Service interface for Push Notification operations.
 */
public interface PushNotificationService {

    /**
     * Registers a client device token for push notifications.
     *
     * @param currentUser authenticated user principal
     * @param request     token registration credentials
     */
    void registerDevice(User currentUser, DeviceTokenRequest request);

    /**
     * Unregisters a client device token.
     *
     * @param currentUser authenticated user principal
     * @param token       the device token string to disable
     */
    void unregisterDevice(User currentUser, String token);

    /**
     * Sends a push notification to all active devices registered by the user.
     *
     * @param userId recipient user identifier
     * @param title  notification title
     * @param body   notification message body
     */
    void sendPushNotification(Long userId, String title, String body);
}

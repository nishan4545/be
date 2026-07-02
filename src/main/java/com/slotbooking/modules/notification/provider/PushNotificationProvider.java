package com.slotbooking.modules.notification.provider;

/**
 * Interface mapping external push notification delivery gateway operations.
 */
public interface PushNotificationProvider {

    /**
     * Sends a push notification to the client device.
     *
     * @param deviceToken target FCM token
     * @param title       notification title
     * @param body        notification text body
     * @throws Exception if delivery fails
     */
    void sendPush(String deviceToken, String title, String body) throws Exception;
}

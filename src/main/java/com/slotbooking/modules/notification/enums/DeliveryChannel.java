package com.slotbooking.modules.notification.enums;

/**
 * Supported delivery channels for notifications.
 */
public enum DeliveryChannel {
    /** Stored database notification for client pull retrieval */
    IN_APP,
    /** Real-time WebSocket connection notification */
    WEBSOCKET,
    /** Email notification via SMTP */
    EMAIL,
    /** Mobile/web push notification via FCM */
    PUSH,
    /** SMS alert via gateway */
    SMS
}

package com.slotbooking.modules.notification.enums;

/**
 * Priority levels for notifications.
 */
public enum NotificationPriority {
    /** Low priority updates */
    LOW,
    /** Normal priority notifications */
    NORMAL,
    /** High priority messages */
    HIGH,
    /** Critical alerts that bypass user silent/opt-out preferences */
    CRITICAL
}

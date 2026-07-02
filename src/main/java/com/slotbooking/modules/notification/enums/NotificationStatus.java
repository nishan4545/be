package com.slotbooking.modules.notification.enums;

/**
 * Lifecycle states of a notification and its delivery logs.
 */
public enum NotificationStatus {
    /** Awaiting queue processor delivery */
    PENDING,
    /** Dispatched to external gateway provider */
    SENT,
    /** Delivery confirmation received from channel gateway */
    DELIVERED,
    /** User marked notification as read (only for IN_APP) */
    READ,
    /** Delivery failed after max retries */
    FAILED,
    /** Cancelled or timed-out notification */
    EXPIRED
}

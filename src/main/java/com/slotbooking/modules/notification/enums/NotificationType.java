package com.slotbooking.modules.notification.enums;

/**
 * Categories of system and user notifications.
 */
public enum NotificationType {
    /** Player account self-registration completed */
    PLAYER_REGISTERED,
    /** Player account approved by an admin */
    PLAYER_APPROVED,
    /** Player account blocked by an admin */
    PLAYER_BLOCKED,

    /** New tournament created */
    TOURNAMENT_CREATED,
    /** Tournament details updated */
    TOURNAMENT_UPDATED,
    /** Tournament cancelled by admin */
    TOURNAMENT_CANCELLED,
    /** Registration period started */
    REGISTRATION_OPEN,
    /** Registration period ended */
    REGISTRATION_CLOSED,

    /** A player joins/books a tournament slot */
    BOOKING_CREATED,
    /** Player booking slot successfully confirmed */
    BOOKING_CONFIRMED,
    /** Player booking slot cancelled */
    BOOKING_CANCELLED,
    /** Player booking slot expired due to payment timeout */
    BOOKING_EXPIRED,

    /** Payment transaction successful */
    PAYMENT_SUCCESS,
    /** Payment transaction failed */
    PAYMENT_FAILED,
    /** Refund processed successfully */
    REFUND_SUCCESS,

    /** Automated reminder before tournament starts */
    TOURNAMENT_REMINDER,
    /** Winner of a tournament declared */
    WINNER_ANNOUNCED,
    
    /** Global administrative broadcast message */
    ADMIN_BROADCAST
}

package com.slotbooking.modules.websocket.dto;

/**
 * Enumeration representing all real-time WebSocket event types.
 */
public enum EventType {
    /** New booking created by a player */
    BOOKING_CREATED,
    /** Booking cancelled by a player or admin */
    BOOKING_CANCELLED,
    /** Booking expired due to payment timeout */
    BOOKING_EXPIRED,
    /** Booking refunded by an admin */
    BOOKING_REFUNDED,

    /** Successful payment transaction confirmed */
    PAYMENT_SUCCESS,
    /** Failed payment transaction */
    PAYMENT_FAILED,
    /** Refund process completed successfully */
    REFUND_SUCCESS,

    /** New player approved by admin */
    PLAYER_APPROVED,
    /** New player self-registered */
    PLAYER_REGISTERED,
    /** Player account blocked by admin */
    PLAYER_BLOCKED,

    /** New tournament created */
    TOURNAMENT_CREATED,
    /** Tournament details or lifecycle updated */
    TOURNAMENT_UPDATED,
    /** Tournament cancelled by admin */
    TOURNAMENT_CANCELLED,

    /** Tournament slot counts changed */
    SLOT_UPDATED,

    /** Player connected to WebSocket */
    PLAYER_ONLINE,
    /** Player disconnected from WebSocket */
    PLAYER_OFFLINE,
    /** Administrator connected to WebSocket */
    ADMIN_ONLINE,
    /** Administrator disconnected from WebSocket */
    ADMIN_OFFLINE,

    /** Real-time statistics metrics update */
    METRICS_UPDATE
}

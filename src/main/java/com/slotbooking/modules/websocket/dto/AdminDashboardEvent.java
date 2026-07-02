package com.slotbooking.modules.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event DTO representing real-time system events sent to the admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardEvent {

    /** Event type: e.g., PLAYER_REGISTERED, PLAYER_APPROVED, PLAYER_BLOCKED, BOOKING_CREATED, PAYMENT_SUCCESS, TOURNAMENT_FULL, REFUND_COMPLETED */
    private EventType eventType;

    /** Human-readable event description */
    private String description;

    /** Unique identifier of the resource associated (user ID, booking ID, payment ID, etc.) */
    private Long resourceId;

    /** Formatted ISO timestamp when event occurred */
    private String timestamp;
}

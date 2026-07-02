package com.slotbooking.modules.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Event DTO representing player booking updates sent privately to individual sessions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {

    /** Event type: e.g., CONFIRMED, CANCELLED, EXPIRED, REFUNDED */
    private EventType eventType;

    /** Unique identifier of the booking */
    private Long bookingId;

    /** Associated tournament identifier */
    private Long tournamentId;

    /** Associated tournament title */
    private String tournamentTitle;

    /** Date the tournament is scheduled */
    private LocalDate tournamentDate;

    /** Current booking status */
    private String status;
}

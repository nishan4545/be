package com.slotbooking.modules.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Event DTO representing tournament lifecycle updates broadcasted to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentEvent {

    /** Event type: e.g., CREATED, UPDATED, CANCELLED, REGISTRATION_OPEN, REGISTRATION_CLOSED, STARTED, COMPLETED */
    private EventType eventType;

    /** Unique identifier of the tournament */
    private Long tournamentId;

    /** Title of the tournament */
    private String title;

    /** Date the tournament is scheduled */
    private LocalDate tournamentDate;

    /** Current lifecycle status */
    private String status;
}

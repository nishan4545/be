package com.slotbooking.modules.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event DTO representing real-time tournament slot count updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotUpdateEvent {

    /** Unique identifier of the tournament */
    private Long tournamentId;

    /** Available remaining slots */
    private Integer availableSlots;

    /** Total configured slots for the tournament */
    private Integer totalSlots;
}

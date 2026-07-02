package com.slotbooking.modules.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO to join a tournament.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    /** The identifier of the tournament to join */
    @NotNull(message = "Tournament ID must not be null")
    private Long tournamentId;
}

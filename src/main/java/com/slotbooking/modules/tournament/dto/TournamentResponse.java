package com.slotbooking.modules.tournament.dto;

import com.slotbooking.modules.tournament.enums.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for tournament data returned to clients.
 * Maps from the Tournament entity, exposing only public-facing fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResponse {

    private Long id;
    private String title;
    private String description;
    private String bannerImage;
    private LocalDateTime registrationStartDate;
    private LocalDateTime registrationEndDate;
    private LocalDate tournamentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalSlots;
    private Integer availableSlots;
    private BigDecimal entryFee;
    private BigDecimal prizeAmount;
    private Integer winnerCount;
    private TournamentStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

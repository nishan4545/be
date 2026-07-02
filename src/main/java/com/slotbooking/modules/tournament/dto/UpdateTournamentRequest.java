package com.slotbooking.modules.tournament.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Request DTO for updating an existing tournament.
 * All fields are optional — only provided fields will be updated.
 */
@Data
public class UpdateTournamentRequest {

    private String title;

    private String description;

    private String bannerImage;

    private LocalDateTime registrationStartDate;

    private LocalDateTime registrationEndDate;

    private LocalDate tournamentDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Min(value = 1, message = "Total slots must be at least 1")
    private Integer totalSlots;

    @DecimalMin(value = "0.0", message = "Entry fee must be zero or positive")
    private BigDecimal entryFee;

    @DecimalMin(value = "0.0", message = "Prize amount must be zero or positive")
    private BigDecimal prizeAmount;

    @Min(value = 1, message = "Winner count must be at least 1")
    private Integer winnerCount;
}

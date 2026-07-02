package com.slotbooking.modules.tournament.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Request DTO for creating a new tournament.
 * All business validations are enforced at the service layer
 * in addition to these annotation constraints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTournamentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String bannerImage;

    @NotNull(message = "Registration start date is required")
    private LocalDateTime registrationStartDate;

    @NotNull(message = "Registration end date is required")
    private LocalDateTime registrationEndDate;

    @NotNull(message = "Tournament date is required")
    private LocalDate tournamentDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @NotNull(message = "Total slots is required")
    @Min(value = 1, message = "Total slots must be at least 1")
    private Integer totalSlots;

    @NotNull(message = "Entry fee is required")
    @DecimalMin(value = "0.0", message = "Entry fee must be zero or positive")
    private BigDecimal entryFee;

    @NotNull(message = "Prize amount is required")
    @DecimalMin(value = "0.0", message = "Prize amount must be zero or positive")
    private BigDecimal prizeAmount;

    @NotNull(message = "Winner count is required")
    @Min(value = 1, message = "Winner count must be at least 1")
    private Integer winnerCount;
}

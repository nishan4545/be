package com.slotbooking.modules.booking.dto;

import com.slotbooking.modules.booking.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO returning complete details of a player booking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    /** The unique identifier of the booking */
    private Long id;

    /** Player details */
    private Long userId;
    private String playerFullName;
    private String playerMobileNumber;

    /** Tournament details */
    private Long tournamentId;
    private String tournamentTitle;
    private LocalDate tournamentDate;

    /** Current status of the booking */
    private BookingStatus status;

    private Integer seatNumber;

    /** Audit fields */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

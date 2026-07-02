package com.slotbooking.modules.booking.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.booking.dto.BookingResponse;
import com.slotbooking.modules.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for admin booking reporting.
 * Base URL is /api/admin/bookings. Restricted to ROLE_ADMIN via SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    /**
     * Lists all bookings registered in the system.
     *
     * @return response entity wrapping the full list of bookings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved all bookings", bookings));
    }

    /**
     * Lists all bookings associated with a specific tournament.
     *
     * @param tournamentId the identifier of the tournament
     * @return response entity wrapping the list of bookings for the tournament
     */
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTournamentBookings(
            @PathVariable Long tournamentId
    ) {
        List<BookingResponse> bookings = bookingService.getTournamentBookings(tournamentId);
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved tournament bookings", bookings));
    }

    /**
     * Lists all bookings (booking history) registered by a specific player.
     *
     * @param playerId the identifier of the player
     * @return response entity wrapping the list of bookings for the player
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getPlayerBookingHistory(
            @PathVariable Long playerId
    ) {
        List<BookingResponse> bookings = bookingService.getPlayerBookingHistory(playerId);
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved player booking history", bookings));
    }
}

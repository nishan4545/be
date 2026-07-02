package com.slotbooking.modules.booking.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.booking.dto.BookingResponse;
import com.slotbooking.modules.booking.dto.BookingSummaryResponse;
import com.slotbooking.modules.booking.dto.CreateBookingRequest;
import com.slotbooking.modules.booking.service.BookingService;
import com.slotbooking.modules.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for player booking management.
 * Base URL is /api/player/bookings. Restricted to ROLE_PLAYER via SecurityConfig.
 */
@RestController
@RequestMapping("/api/player/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Registers (joins) the current logged-in player into a tournament slot.
     *
     * @param currentUser the authenticated player
     * @param request     the create booking request details
     * @return response entity wrapping the booking registration details
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> joinTournament(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid CreateBookingRequest request
    ) {
        BookingResponse response = bookingService.joinTournament(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Successfully joined the tournament", response));
    }

    /**
     * Retrieves all bookings for the currently authenticated player.
     *
     * @param currentUser the authenticated player
     * @return response entity wrapping the player's bookings list
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingSummaryResponse>>> getMyBookings(
            @AuthenticationPrincipal User currentUser
    ) {
        List<BookingSummaryResponse> response = bookingService.getMyBookings(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved your bookings", response));
    }

    /**
     * Retrieves detailed information of a booking by ID.
     * Checks that the booking belongs to the current user.
     *
     * @param currentUser the authenticated player
     * @param id          the identifier of the booking
     * @return response entity wrapping the detailed booking response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id
    ) {
        BookingResponse response = bookingService.getBookingDetails(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved booking details", response));
    }

    /**
     * Cancels an existing booking, freeing up the tournament slot.
     * Only permitted if the tournament has not yet started.
     *
     * @param currentUser the authenticated player
     * @param id          the identifier of the booking to cancel
     * @return response entity indicating cancellation success
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id
    ) {
        bookingService.cancelBooking(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", null));
    }
}

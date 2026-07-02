package com.slotbooking.modules.booking.service;

import com.slotbooking.modules.booking.dto.BookingResponse;
import com.slotbooking.modules.booking.dto.BookingSummaryResponse;
import com.slotbooking.modules.booking.dto.CreateBookingRequest;
import com.slotbooking.modules.user.entity.User;

import java.util.List;

/**
 * Service interface for booking operations.
 * Handles tournament booking registration, cancellation, retrieval, and reporting.
 */
public interface BookingService {

    /**
     * Registers (joins) a player to a tournament.
     * Enforces slot allocations, locking, validation of registration time, status checks, and duplicates.
     *
     * @param currentUser the authenticated player user principal
     * @param request     contains tournament id
     * @return the created booking response
     */
    BookingResponse joinTournament(User currentUser, CreateBookingRequest request);

    /**
     * Cancels an existing tournament booking.
     * Restores the slot to the tournament and sets booking status to CANCELLED.
     *
     * @param currentUser the authenticated player user principal
     * @param bookingId   the identifier of the booking to cancel
     */
    void cancelBooking(User currentUser, Long bookingId);

    /**
     * Retrieves all bookings for the currently logged-in player.
     *
     * @param currentUser the authenticated player
     * @return list of booking summaries
     */
    List<BookingSummaryResponse> getMyBookings(User currentUser);

    /**
     * Retrieves detailed information of a booking.
     * Confirms the booking belongs to the current user or that the current user is an admin.
     *
     * @param currentUser the authenticated user principal
     * @param bookingId   the identifier of the booking
     * @return detailed booking response DTO
     */
    BookingResponse getBookingDetails(User currentUser, Long bookingId);

    /**
     * Retrieves all bookings in the system.
     * Accessible by admins.
     *
     * @return list of all bookings
     */
    List<BookingResponse> getAllBookings();

    /**
     * Retrieves all bookings for a specific tournament.
     * Accessible by admins.
     *
     * @param tournamentId the tournament identifier
     * @return list of bookings for the tournament
     */
    List<BookingResponse> getTournamentBookings(Long tournamentId);

    /**
     * Retrieves booking history for a specific player.
     * Accessible by admins.
     *
     * @param playerId the player identifier
     * @return list of bookings registered by the player
     */
    List<BookingResponse> getPlayerBookingHistory(Long playerId);
}

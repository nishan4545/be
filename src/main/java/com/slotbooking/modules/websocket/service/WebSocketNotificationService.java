package com.slotbooking.modules.websocket.service;

import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.payment.entity.Payment;
import com.slotbooking.modules.tournament.entity.Tournament;

/**
 * Service interface defining WebSocket event distribution.
 * Pushes real-time updates to public channels, player private queues, and admin monitors.
 */
public interface WebSocketNotificationService {

    /**
     * Broadcasts a tournament creation event.
     * Destination: /topic/tournaments
     *
     * @param tournament the created tournament details
     */
    void notifyTournamentCreated(Tournament tournament);

    /**
     * Broadcasts a tournament update event.
     * Destination: /topic/tournaments and /topic/admin/tournaments
     *
     * @param tournament the updated tournament details
     */
    void notifyTournamentUpdated(Tournament tournament);

    /**
     * Broadcasts a tournament cancellation event.
     * Destination: /topic/tournaments and /topic/admin/tournaments
     *
     * @param tournament the cancelled tournament details
     */
    void notifyTournamentCancelled(Tournament tournament);

    /**
     * Broadcasts a tournament slot count changed event.
     * Destination: /topic/tournament/{tournamentId}/slots and /topic/admin/tournaments
     *
     * @param tournament the tournament with updated slot counts
     */
    void notifySlotUpdated(Tournament tournament);

    /**
     * Sends a private booking confirmation event to the specific player session.
     * Destination: /user/queue/bookings (mapped to player session)
     *
     * @param booking the confirmed booking details
     */
    void notifyBookingConfirmed(Booking booking);

    /**
     * Sends a private booking cancellation event to the specific player session.
     * Destination: /user/queue/bookings (mapped to player session)
     *
     * @param booking the cancelled booking details
     */
    void notifyBookingCancelled(Booking booking);

    /**
     * Sends a private booking expiration event to the specific player session.
     * Destination: /user/queue/bookings (mapped to player session)
     *
     * @param booking the expired booking details
     */
    void notifyBookingExpired(Booking booking);

    /**
     * Sends a private payment successful event to the specific player session.
     * Destination: /user/queue/payments (mapped to player session)
     *
     * @param payment the successful payment transaction details
     */
    void notifyPaymentSuccess(Payment payment);

    /**
     * Sends a private payment failure event to the specific player session.
     * Destination: /user/queue/payments (mapped to player session)
     *
     * @param payment the failed payment transaction details
     */
    void notifyPaymentFailed(Payment payment);

    /**
     * Sends a private payment refund event to the specific player session.
     * Destination: /user/queue/payments (mapped to player session)
     *
     * @param payment the refunded payment transaction details
     */
    void notifyRefund(Payment payment);

    /**
     * Broadcasts an administrative event to the admin dashboard.
     * Destination: /topic/admin/dashboard
     *
     * @param eventType   the system event classification
     * @param description text description of the event
     * @param resourceId  the identifier of the changed entity
     */
    void notifyAdminDashboard(String eventType, String description, Long resourceId);
}

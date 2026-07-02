package com.slotbooking.modules.websocket.service;

import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.payment.entity.Payment;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.websocket.dto.*;
import com.slotbooking.modules.websocket.listener.WebSocketEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service implementation for WebSocket notifications.
 * Dispatches structured, asynchronous events to clients and updates admin stats.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    
    // Inject event listener lazily to prevent startup dependency cycles
    @Lazy
    private final WebSocketEventListener webSocketEventListener;

    @Async
    @Override
    public void notifyTournamentCreated(Tournament tournament) {
        TournamentEvent event = TournamentEvent.builder()
                .eventType(EventType.TOURNAMENT_CREATED)
                .tournamentId(tournament.getId())
                .title(tournament.getTitle())
                .tournamentDate(tournament.getTournamentDate())
                .status(tournament.getStatus().name())
                .build();

        try {
            log.info("[WS BROADCAST] Pushing Tournament Created event for ID: {}", tournament.getId());
            messagingTemplate.convertAndSend("/topic/tournaments", event);
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on /topic/tournaments: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifyTournamentUpdated(Tournament tournament) {
        TournamentEvent event = TournamentEvent.builder()
                .eventType(EventType.TOURNAMENT_UPDATED)
                .tournamentId(tournament.getId())
                .title(tournament.getTitle())
                .tournamentDate(tournament.getTournamentDate())
                .status(tournament.getStatus().name())
                .build();

        try {
            log.info("[WS BROADCAST] Pushing Tournament Updated event for ID: {}", tournament.getId());
            messagingTemplate.convertAndSend("/topic/tournaments", event);
            messagingTemplate.convertAndSend("/topic/admin/tournaments", event);
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on tournament update notifications: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifyTournamentCancelled(Tournament tournament) {
        TournamentEvent event = TournamentEvent.builder()
                .eventType(EventType.TOURNAMENT_CANCELLED)
                .tournamentId(tournament.getId())
                .title(tournament.getTitle())
                .tournamentDate(tournament.getTournamentDate())
                .status(tournament.getStatus().name())
                .build();

        try {
            log.info("[WS BROADCAST] Pushing Tournament Cancelled event for ID: {}", tournament.getId());
            messagingTemplate.convertAndSend("/topic/tournaments", event);
            messagingTemplate.convertAndSend("/topic/admin/tournaments", event);
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on tournament cancellation notifications: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifySlotUpdated(Tournament tournament) {
        SlotUpdateEvent event = SlotUpdateEvent.builder()
                .tournamentId(tournament.getId())
                .availableSlots(tournament.getAvailableSlots())
                .totalSlots(tournament.getTotalSlots())
                .build();

        try {
            String destination = "/topic/tournament/" + tournament.getId() + "/slots";
            log.info("[WS BROADCAST] Pushing Slot count updated for tournament: {}, slots: {}", tournament.getId(), tournament.getAvailableSlots());
            messagingTemplate.convertAndSend(destination, event);
            
            // Push slot change alert to admin monitor
            AdminDashboardEvent adminEvent = AdminDashboardEvent.builder()
                    .eventType(EventType.SLOT_UPDATED)
                    .description("Tournament '" + tournament.getTitle() + "' slots updated. Available: " + tournament.getAvailableSlots())
                    .resourceId(tournament.getId())
                    .timestamp(LocalDateTimeFormatter.nowFormatted())
                    .build();
            messagingTemplate.convertAndSend("/topic/admin/tournaments", adminEvent);
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on slot updates: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifyBookingConfirmed(Booking booking) {
        BookingEvent event = BookingEvent.builder()
                .eventType(EventType.BOOKING_CREATED)
                .bookingId(booking.getId())
                .tournamentId(booking.getTournament().getId())
                .tournamentTitle(booking.getTournament().getTitle())
                .tournamentDate(booking.getTournament().getTournamentDate())
                .status(booking.getStatus().name())
                .build();

        try {
            String username = booking.getUser().getMobileNumber();
            log.info("[WS BROADCAST] Pushing booking confirmation to user: {}", username);
            messagingTemplate.convertAndSendToUser(username, "/queue/bookings", event);
            
            // Sync metrics to admin dashboard
            webSocketEventListener.broadcastMetrics();
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on booking confirmation to user queue: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifyBookingCancelled(Booking booking) {
        BookingEvent event = BookingEvent.builder()
                .eventType(EventType.BOOKING_CANCELLED)
                .bookingId(booking.getId())
                .tournamentId(booking.getTournament().getId())
                .tournamentTitle(booking.getTournament().getTitle())
                .tournamentDate(booking.getTournament().getTournamentDate())
                .status(booking.getStatus().name())
                .build();

        try {
            String username = booking.getUser().getMobileNumber();
            log.info("[WS BROADCAST] Pushing booking cancellation to user: {}", username);
            messagingTemplate.convertAndSendToUser(username, "/queue/bookings", event);
            
            // Sync metrics to admin dashboard
            webSocketEventListener.broadcastMetrics();
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on booking cancellation to user queue: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifyBookingExpired(Booking booking) {
        BookingEvent event = BookingEvent.builder()
                .eventType(EventType.BOOKING_EXPIRED)
                .bookingId(booking.getId())
                .tournamentId(booking.getTournament().getId())
                .tournamentTitle(booking.getTournament().getTitle())
                .tournamentDate(booking.getTournament().getTournamentDate())
                .status(booking.getStatus().name())
                .build();

        try {
            String username = booking.getUser().getMobileNumber();
            log.info("[WS BROADCAST] Pushing booking expiration to user: {}", username);
            messagingTemplate.convertAndSendToUser(username, "/queue/bookings", event);
            
            // Sync metrics to admin dashboard
            webSocketEventListener.broadcastMetrics();
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on booking expiration to user queue: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifyPaymentSuccess(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType(EventType.PAYMENT_SUCCESS)
                .paymentId(payment.getId())
                .bookingId(payment.getBooking().getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .build();

        try {
            String username = payment.getBooking().getUser().getMobileNumber();
            log.info("[WS BROADCAST] Pushing payment success event to user: {}", username);
            messagingTemplate.convertAndSendToUser(username, "/queue/payments", event);
            
            // Sync metrics to admin dashboard
            webSocketEventListener.broadcastMetrics();
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on payment success to user queue: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifyPaymentFailed(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType(EventType.PAYMENT_FAILED)
                .paymentId(payment.getId())
                .bookingId(payment.getBooking().getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .build();

        try {
            String username = payment.getBooking().getUser().getMobileNumber();
            log.info("[WS BROADCAST] Pushing payment failure event to user: {}", username);
            messagingTemplate.convertAndSendToUser(username, "/queue/payments", event);
            
            // Sync metrics to admin dashboard
            webSocketEventListener.broadcastMetrics();
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on payment failure to user queue: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifyRefund(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType(EventType.REFUND_SUCCESS)
                .paymentId(payment.getId())
                .bookingId(payment.getBooking().getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .build();

        try {
            String username = payment.getBooking().getUser().getMobileNumber();
            log.info("[WS BROADCAST] Pushing refund success event to user: {}", username);
            messagingTemplate.convertAndSendToUser(username, "/queue/payments", event);
            
            // Sync metrics to admin dashboard
            webSocketEventListener.broadcastMetrics();
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on refund success to user queue: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void notifyAdminDashboard(String eventType, String description, Long resourceId) {
        EventType type = EventType.valueOf(eventType);
        AdminDashboardEvent event = AdminDashboardEvent.builder()
                .eventType(type)
                .description(description)
                .resourceId(resourceId)
                .timestamp(LocalDateTimeFormatter.nowFormatted())
                .build();

        try {
            log.info("[WS BROADCAST] Pushing Dashboard Event: {}", type);
            messagingTemplate.convertAndSend("/topic/admin/dashboard", event);
        } catch (Exception e) {
            log.error("[WS ERROR] Broadcast failure on admin dashboard: {}", e.getMessage());
        }
    }

    /**
     * Local helper class to format current timestamp for dashboard events.
     */
    private static class LocalDateTimeFormatter {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        public static String nowFormatted() {
            return java.time.LocalDateTime.now().format(formatter);
        }
    }
}

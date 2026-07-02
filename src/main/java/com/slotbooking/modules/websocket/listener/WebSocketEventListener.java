package com.slotbooking.modules.websocket.listener;

import com.slotbooking.modules.booking.repository.BookingRepository;
import com.slotbooking.modules.payment.enums.PaymentStatus;
import com.slotbooking.modules.payment.repository.PaymentRepository;
import com.slotbooking.modules.websocket.dto.AdminDashboardEvent;
import com.slotbooking.modules.websocket.dto.EventType;
import com.slotbooking.modules.websocket.dto.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event listener tracking active user connections, subscriptions, and disconnections.
 * Updates online user counts, logs STOMP frame routing, and broadcasts real-time dashboard metrics to admins.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private static final ConcurrentHashMap<String, UserSession> activeSessions = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Gets the current count of online users matching a specific role.
     *
     * @param roleName target authority role
     * @return current active session count
     */
    public static long getOnlineCountByRole(String roleName) {
        return activeSessions.values().stream()
                .filter(session -> session.getRole().equals(roleName))
                .count();
    }

    /**
     * Gets the count of active subscriptions for a given session.
     *
     * @param sessionId the session identifier
     * @return count of subscriptions
     */
    public static long getSessionSubscriptionCount(String sessionId) {
        // Can be extended or used for rate limiting check
        return 0; // standard placeholder
    }

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal != null) {
            String sessionId = accessor.getSessionId();
            String username = principal.getName();
            
            String role = "ROLE_PLAYER";
            if (principal instanceof UsernamePasswordAuthenticationToken token) {
                role = token.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("ROLE_PLAYER");
            }

            UserSession session = UserSession.builder()
                    .userId(username)
                    .role(role)
                    .sessionId(sessionId)
                    .ipAddress(accessor.getHost() != null ? accessor.getHost() : "127.0.0.1")
                    .connectedTime(System.currentTimeMillis())
                    .build();

            activeSessions.put(sessionId, session);

            log.info("[WS CONNECT] User ID: {}, Role: {}, Session ID: {}", username, role, sessionId);
            
            broadcastPresence(session, true);
            broadcastMetrics();
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        UserSession session = activeSessions.remove(sessionId);
        
        if (session != null) {
            log.info("[WS DISCONNECT] User ID: {}, Session ID: {}", session.getUserId(), sessionId);
            
            broadcastPresence(session, false);
            broadcastMetrics();
        }
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = accessor.getUser() != null ? accessor.getUser().getName() : "Anonymous";
        log.info("[WS SUBSCRIBE] User: {} subscribed to: {}, Session ID: {}", username, accessor.getDestination(), accessor.getSessionId());
    }

    /**
     * Broadcasts client presence status (online/offline) to admin dashboard.
     */
    private void broadcastPresence(UserSession session, boolean isOnline) {
        boolean isAdmin = "ROLE_ADMIN".equals(session.getRole());
        EventType eventType;
        String action;

        if (isAdmin) {
            eventType = isOnline ? EventType.ADMIN_ONLINE : EventType.ADMIN_OFFLINE;
            action = isOnline ? "ADMIN_ONLINE" : "ADMIN_OFFLINE";
        } else {
            eventType = isOnline ? EventType.PLAYER_ONLINE : EventType.PLAYER_OFFLINE;
            action = isOnline ? "PLAYER_ONLINE" : "PLAYER_OFFLINE";
        }

        String description = String.format("%s: User %s is now %s", action, session.getUserId(), isOnline ? "online" : "offline");

        AdminDashboardEvent presenceEvent = AdminDashboardEvent.builder()
                .eventType(eventType)
                .description(description)
                .resourceId(0L)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        try {
            log.info("[WS BROADCAST] Pushing Presence Event: {}", eventType);
            messagingTemplate.convertAndSend("/topic/admin/dashboard", presenceEvent);
        } catch (Exception e) {
            log.error("[WS ERROR] Failed to send presence event", e);
        }
    }

    /**
     * Calculates and broadcasts today's admin metrics dashboard details.
     */
    public void broadcastMetrics() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        long onlinePlayers = getOnlineCountByRole("ROLE_PLAYER");
        long onlineAdmins = getOnlineCountByRole("ROLE_ADMIN");
        long todayBookings = bookingRepository.countByCreatedAtAfter(startOfDay);
        long todayPayments = paymentRepository.countByStatusAndCreatedAtAfter(PaymentStatus.SUCCESS, startOfDay);
        BigDecimal todayRevenue = paymentRepository.sumRevenueSince(startOfDay);

        String description = String.format(
                "{\"onlinePlayers\":%d,\"onlineAdmins\":%d,\"todayBookings\":%d,\"todayPayments\":%d,\"todayRevenue\":%s}",
                onlinePlayers, onlineAdmins, todayBookings, todayPayments, todayRevenue.toPlainString()
        );

        AdminDashboardEvent metricsEvent = AdminDashboardEvent.builder()
                .eventType(EventType.METRICS_UPDATE)
                .description(description)
                .resourceId(0L)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        try {
            log.info("[WS BROADCAST] Pushing Metrics Update to Dashboard");
            messagingTemplate.convertAndSend("/topic/admin/dashboard", metricsEvent);
        } catch (Exception e) {
            log.error("[WS ERROR] Failed to send metrics event", e);
        }
    }
}

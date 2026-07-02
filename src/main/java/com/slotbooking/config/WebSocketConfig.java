package com.slotbooking.config;

import com.slotbooking.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enterprise configuration for WebSocket STOMP message broker.
 * Implements origin validation, size limits, client SEND restrictions, JWT token checks, and connection/subscription rate limits.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${WEBSOCKET_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000}")
    private String[] allowedOrigins;

    // Rate limiting tracking structures
    private static final ConcurrentHashMap<String, List<Long>> ipConnectionTimestamps = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();

        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(taskScheduler);

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024);       // 64 KB
        registration.setSendBufferSizeLimit(512 * 1024);   // 512 KB
        registration.setSendTimeLimit(20000);              // 20 seconds
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }

                // 1. WebSocket Client SEND Command Restriction
                if (StompCommand.SEND.equals(accessor.getCommand())) {
                    log.warn("[WS ERROR] SEND command blocked. Client tried sending message to: {}", accessor.getDestination());
                    throw new MessageDeliveryException("SEND commands are disabled on this broker. Clients are subscribe-only.");
                }

                // 2. JWT Connection Authentication and IP Rate Limiting on CONNECT
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String ip = accessor.getHost() != null ? accessor.getHost() : "unknown-ip";
                    
                    // Connection Rate Limiting check (Max 5 connections/second per IP)
                    long now = System.currentTimeMillis();
                    ipConnectionTimestamps.compute(ip, (key, list) -> {
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        list.removeIf(timestamp -> now - timestamp > 1000);
                        return list;
                    });
                    
                    List<Long> timestamps = ipConnectionTimestamps.get(ip);
                    if (timestamps != null && timestamps.size() >= 5) {
                        log.warn("[WS ERROR] Connection rate limit exceeded for IP: {}", ip);
                        throw new MessageDeliveryException("Connection rate limit exceeded. Max 5 connections/second allowed.");
                    }
                    timestamps.add(now);

                    List<String> authorization = accessor.getNativeHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        String bearerToken = authorization.get(0);
                        String jwt = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
                        try {
                            String mobileNumber = jwtService.extractUsername(jwt);
                            if (mobileNumber != null) {
                                UserDetails userDetails = userDetailsService.loadUserByUsername(mobileNumber);
                                if (jwtService.isTokenValid(jwt, userDetails)) {
                                    UsernamePasswordAuthenticationToken authentication =
                                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                    accessor.setUser(authentication);
                                    
                                    log.info("[WS CONNECT] Connection validated successfully for: {}", mobileNumber);
                                }
                            }
                        } catch (Exception e) {
                            log.error("[WS ERROR] JWT connection validation failed: {}", e.getMessage());
                            throw new MessageDeliveryException("Unauthorized connection: JWT signature invalid.");
                        }
                    } else {
                        log.warn("[WS ERROR] Connection rejected - Missing Authorization header");
                        throw new MessageDeliveryException("Unauthorized connection: Credentials missing.");
                    }
                }

                // 3. JWT and Subscription Limit check on SUBSCRIBE
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    Principal principal = accessor.getUser();
                    if (principal == null) {
                        log.warn("[WS ERROR] Subscription rejected - Anonymous context");
                        throw new MessageDeliveryException("Access Denied: Anonymous subscriptions are blocked.");
                    }

                    String sessionId = accessor.getSessionId();
                    String destination = accessor.getDestination();
                    if (destination == null || destination.isBlank()) {
                        throw new MessageDeliveryException("Access Denied: Destination is invalid.");
                    }

                    // Subscription Rate Limiting (Max 20 active subscriptions per session)
                    sessionSubscriptions.compute(sessionId, (key, set) -> {
                        if (set == null) {
                            set = ConcurrentHashMap.newKeySet();
                        }
                        return set;
                    });
                    Set<String> subs = sessionSubscriptions.get(sessionId);
                    if (subs != null && subs.size() >= 20) {
                        log.warn("[WS ERROR] Subscription count limit exceeded for Session: {}", sessionId);
                        throw new MessageDeliveryException("Subscription limit exceeded. Max 20 active subscriptions allowed.");
                    }
                    subs.add(destination);

                    UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
                    UserDetails userDetails = (UserDetails) authToken.getPrincipal();
                    boolean isAdmin = userDetails.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                    boolean isPlayer = userDetails.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLAYER"));

                    log.info("[WS SUBSCRIBE] User: {} subscribing to: {}", userDetails.getUsername(), destination);

                    // Validate Roles based on Subscription Prefix
                    if (destination.startsWith("/topic/admin/") || destination.startsWith("/topic/admin")) {
                        if (!isAdmin) {
                            log.warn("[WS ERROR] Subscription Rejected - User {} lacks Admin role for: {}", userDetails.getUsername(), destination);
                            throw new MessageDeliveryException("Access Denied: Admin role required.");
                        }
                    }

                    if (destination.startsWith("/queue/") || destination.startsWith("/topic/tournament/") || destination.startsWith("/user/")) {
                        if (!isPlayer && !isAdmin) {
                            log.warn("[WS ERROR] Subscription Rejected - User {} lacks Player role for: {}", userDetails.getUsername(), destination);
                            throw new MessageDeliveryException("Access Denied: Player role required.");
                        }
                    }
                }

                // 4. Subscription Tracking Cleanups on UNSUBSCRIBE or DISCONNECT
                if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
                    String sessionId = accessor.getSessionId();
                    String destination = accessor.getDestination();
                    Set<String> subs = sessionSubscriptions.get(sessionId);
                    if (subs != null && destination != null) {
                        subs.remove(destination);
                    }
                }

                if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    String sessionId = accessor.getSessionId();
                    sessionSubscriptions.remove(sessionId);
                }

                return message;
            }
        });
    }
}

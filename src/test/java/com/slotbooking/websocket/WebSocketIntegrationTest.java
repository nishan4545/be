package com.slotbooking.websocket;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebSocketIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    private WebSocketStompClient stompClient;
    private String jwtToken;

    @BeforeEach
    void setup() {
        User player = User.builder()
                .id(1L)
                .mobileNumber("9876543299")
                .role(Role.PLAYER)
                .build();
        jwtToken = jwtService.generateToken(player);

        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void testConnectAndSubscribe() throws Exception {
        String url = "ws://localhost:" + port + "/ws";
        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);

        CompletableFuture<StompSession> completableFuture = new CompletableFuture<>();

        stompClient.connectAsync(url, new WebSocketHttpHeaders(), headers, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                completableFuture.complete(session);
            }
        });

        StompSession session = completableFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(session);
        assertTrue(session.isConnected());
        session.disconnect();
    }
}

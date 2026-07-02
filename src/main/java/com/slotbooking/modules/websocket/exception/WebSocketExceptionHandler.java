package com.slotbooking.modules.websocket.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Global exception handler advice for STOMP message mappings.
 * Intercepts handler exceptions, logs error states, and sends safe messages back to user queue error channels.
 */
@ControllerAdvice
@Slf4j
public class WebSocketExceptionHandler {

    /**
     * Intercepts message exceptions and transmits safe messaging response.
     *
     * @param exception the exception instance encountered
     * @return safe text error description
     */
    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public String handleException(Exception exception) {
        log.error("[WS ERROR] WebSocket exception caught: {}", exception.getMessage());
        return "Error: " + exception.getMessage();
    }
}

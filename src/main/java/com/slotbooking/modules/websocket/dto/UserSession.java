package com.slotbooking.modules.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data structure tracking metadata of active user WebSocket connections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    /** The authenticated mobile number / user identifier */
    private String userId;

    /** Security authority roles assigned to the user (e.g. ROLE_PLAYER, ROLE_ADMIN) */
    private String role;

    /** Unique WebSocket connection session identifier */
    private String sessionId;

    /** Cryptographic user IP address */
    private String ipAddress;

    /** Connection establishment epoch timestamp */
    private long connectedTime;
}

package com.slotbooking.modules.notification.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.notification.dto.DeviceTokenRequest;
import com.slotbooking.modules.notification.service.PushNotificationService;
import com.slotbooking.modules.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for player client push notification device token registrations.
 * Base URL is /api/player/device. Restricted to ROLE_PLAYER.
 */
@RestController
@RequestMapping("/api/player/device")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final PushNotificationService pushNotificationService;

    /**
     * Registers a device token.
     *
     * @param currentUser authenticated player
     * @param request     device token registration payload
     * @return empty response entity indicating success
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerDevice(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid DeviceTokenRequest request
    ) {
        pushNotificationService.registerDevice(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Device token registered successfully", null));
    }

    /**
     * Unregisters/deactivates a device token.
     *
     * @param currentUser authenticated player
     * @param token       device token key to disable
     * @return empty response entity indicating success
     */
    @DeleteMapping("/{token}")
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String token
    ) {
        pushNotificationService.unregisterDevice(currentUser, token);
        return ResponseEntity.ok(ApiResponse.success("Device token unregistered successfully", null));
    }
}

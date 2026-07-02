package com.slotbooking.modules.notification.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.notification.dto.NotificationPreferenceRequest;
import com.slotbooking.modules.notification.dto.NotificationPreferenceResponse;
import com.slotbooking.modules.notification.service.PreferenceService;
import com.slotbooking.modules.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for player notification preferences.
 * Base URL is /api/player/notification-preferences. Restricted to ROLE_PLAYER.
 */
@RestController
@RequestMapping("/api/player/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final PreferenceService preferenceService;

    /**
     * Gets notification preferences for the logged-in player.
     *
     * @param currentUser authenticated player
     * @return response entity wrapping preferences details
     */
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            @AuthenticationPrincipal User currentUser
    ) {
        NotificationPreferenceResponse preferences = preferenceService.getPreferences(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Notification preferences retrieved", preferences));
    }

    /**
     * Updates notification preferences for the player.
     *
     * @param currentUser authenticated player
     * @param request     preferences update flags
     * @return response entity wrapping updated preference details
     */
    @PutMapping
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid NotificationPreferenceRequest request
    ) {
        NotificationPreferenceResponse updated = preferenceService.updatePreferences(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated", updated));
    }
}

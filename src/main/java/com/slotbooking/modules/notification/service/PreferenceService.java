package com.slotbooking.modules.notification.service;

import com.slotbooking.modules.notification.dto.NotificationPreferenceRequest;
import com.slotbooking.modules.notification.dto.NotificationPreferenceResponse;
import com.slotbooking.modules.user.entity.User;

/**
 * Service interface managing user notification opt-in/opt-out channel preferences.
 */
public interface PreferenceService {

    /**
     * Retrieves preferences for the authenticated player.
     * Creates defaults if preferences record does not exist.
     *
     * @param currentUser authenticated user principal
     * @return player channel preferences response DTO
     */
    NotificationPreferenceResponse getPreferences(User currentUser);

    /**
     * Updates channel preferences for the player.
     *
     * @param currentUser authenticated user principal
     * @param request     updated flags flags
     * @return player updated preferences response DTO
     */
    NotificationPreferenceResponse updatePreferences(User currentUser, NotificationPreferenceRequest request);
}

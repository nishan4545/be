package com.slotbooking.modules.notification.service;

import com.slotbooking.modules.notification.dto.NotificationPreferenceRequest;
import com.slotbooking.modules.notification.dto.NotificationPreferenceResponse;
import com.slotbooking.modules.notification.entity.NotificationPreference;
import com.slotbooking.modules.notification.repository.NotificationPreferenceRepository;
import com.slotbooking.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for PreferenceService.
 * Controls loading, updating, and initializing default opt-in preferences.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PreferenceServiceImpl implements PreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Override
    @Transactional
    public NotificationPreferenceResponse getPreferences(User currentUser) {
        NotificationPreference preference = preferenceRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createDefaultPreference(currentUser));
                
        return mapToResponse(preference);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updatePreferences(User currentUser, NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createDefaultPreference(currentUser));

        if (request.getEmailEnabled() != null) {
            preference.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getPushEnabled() != null) {
            preference.setPushEnabled(request.getPushEnabled());
        }
        if (request.getSmsEnabled() != null) {
            preference.setSmsEnabled(request.getSmsEnabled());
        }
        if (request.getWebsocketEnabled() != null) {
            preference.setWebsocketEnabled(request.getWebsocketEnabled());
        }
        if (request.getInAppEnabled() != null) {
            preference.setInAppEnabled(request.getInAppEnabled());
        }

        NotificationPreference saved = preferenceRepository.save(preference);
        log.info("[Preference Service] Preferences updated for user: {}", currentUser.getMobileNumber());
        return mapToResponse(saved);
    }

    private NotificationPreference createDefaultPreference(User user) {
        NotificationPreference preference = NotificationPreference.builder()
                .user(user)
                .emailEnabled(true)
                .pushEnabled(true)
                .smsEnabled(true)
                .websocketEnabled(true)
                .inAppEnabled(true)
                .build();
                
        log.info("[Preference Service] Creating default preferences for user: {}", user.getMobileNumber());
        return preferenceRepository.save(preference);
    }

    private NotificationPreferenceResponse mapToResponse(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUser().getId())
                .emailEnabled(preference.isEmailEnabled())
                .pushEnabled(preference.isPushEnabled())
                .smsEnabled(preference.isSmsEnabled())
                .websocketEnabled(preference.isWebsocketEnabled())
                .inAppEnabled(preference.isInAppEnabled())
                .build();
    }
}

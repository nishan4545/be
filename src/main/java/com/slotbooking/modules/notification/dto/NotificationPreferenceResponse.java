package com.slotbooking.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Response DTO representing user notification channel preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private UUID id;
    private Long userId;
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean smsEnabled;
    private boolean websocketEnabled;
    private boolean inAppEnabled;
}
